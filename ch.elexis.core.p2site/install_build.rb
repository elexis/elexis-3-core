#!/usr/bin/env ruby
#encoding: utf-8
# ========================================================================
# Copyright (c) 2006-2010 Intalio Inc
# ------------------------------------------------------------------------
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# and Apache License v2.0 which accompanies this distribution.
# The Eclipse Public License is available at
# http://www.eclipse.org/legal/epl-v10.html
# The Apache License v2.0 is available at
# http://www.opensource.org/licenses/apache2.0.php
# You may elect to redistribute this code under either of these licenses.
# ========================================================================
#
# Based on Sources published by:
# Author hmalphettes
# github.com/intalio/tycho-p2-scripts
#
# Adapted for the needs of Elexis by Niklaus Giger, niklaus.giger@member.fsf.org 2013
#

require "find"
require "erb"
require "pathname"
require "fileutils"
require 'set'
require 'pp'

class CompositeRepository
  def initialize(output, version, basefolder, name, test=false)
    @outputPath = Pathname.new(output).expand_path
    @basefolder = Pathname.new(basefolder).expand_path
    @name = name
    @version = version
    @test = test

    #contain the list of relative path to the linked versioned repos
    @children_repo = [ ]

    #contains the parent folders of each repo already in the composite repo so we don't duplicate
    @already_indexed_parents = Set.new

    #contain the list of relative path to the linked versioned repos
    #according to the last released aggregate repository
    #we read it in the last released composite repo.
    #if nothing has changed then we don't need to make a new release.
    @currently_released_repo = []

    @ArtifactOrMetadata="Artifact"
    @timestamp=Time.now.to_i
    @date=Time.now.utc

    @versionned_output_dir=nil
    compute_versioned_output
    Find.find(@basefolder) do |path|
      if File.basename(path) == 'artifacts.jar'
        add_childrepo File.new(File.dirname(File.dirname(path)))
        next
      end
      if FileTest.directory?(path)
        if File.basename(path)[0] == ?. and File.basename(path) != '.'
          Find.prune
        elsif File.basename(path) == 'plugins' or File.basename(path) == 'features' or File.basename(path) == 'binaries' or File.basename(path) == @name
          Find.prune
        else
          next
        end
      end
    end
  end

  def add_childrepo( compositeMkrepoFile, version_glob="*" )
    if File.directory? compositeMkrepoFile
      compositeRepoParentFolder=Pathname.new compositeMkrepoFile.path
    else
      compositeRepoParentFolder=Pathname.new Pathname.new(File.dirname compositeMkrepoFile).expand_path
    end
    #make it a path relative to the @versionned_output_dir
    relative=compositeRepoParentFolder.relative_path_from(Pathname.new(@versionned_output_dir))
    if relative.nil?
      raise "Could not compute the relative path of #{compositeRepoParentFolder.to_s} from #{Pathname.new(@versionned_output_dir).to_s}"
    end
    last_version, all=compute_last_version(compositeRepoParentFolder,version_glob)
    if last_version.nil?
      raise "Could not locate a version directory in #{compositeRepoParentFolder.to_s}/#{version_glob}"
    end
    nrVersions ||= ENV['P2_MAX_VERSIONS'].to_i
    /snapshot/i.match(compositeRepoParentFolder.to_s) ? nrVersions ||= 3 : nrVersions ||= 0
    addedVersions = 0
    all.sort.reverse.each{|version|
              newVersion = File.join(relative.to_s, version)
              next if @children_repo.index(newVersion)
              artifactJar = File.expand_path(File.join(compositeRepoParentFolder, relative, newVersion, 'artifacts.jar'))
              if nrVersions == 0 or addedVersions < nrVersions
                addedVersions += 1
                puts "Adding #{addedVersions}/#{nrVersions}: #{newVersion} has #{artifactJar} #{File.size(artifactJar)} bytes"
                @children_repo << newVersion
                @already_indexed_parents << compositeRepoParentFolder
              else
                snapshotDir2Delete = File.dirname(artifactJar)
                puts "Removing #{addedVersions}/#{nrVersions} artifactJar #{snapshotDir2Delete}"
                FileUtils.rm_rf(snapshotDir2Delete, :verbose => $VERBOSE)
              end
            }
  end

  def get_versionned_output_dir()
    return @versionned_output_dir
  end
  def get_version()
    return @version
  end
  def is_changed()
    return @currently_released_repo.nil? || @currently_released_repo.empty? || @currently_released_repo != @children_repo.sort!
  end

  def get_binding
    binding
  end

  def set_ArtifactOrMetaData(artifactOrMetadata)
    @ArtifactOrMetadata=artifactOrMetadata
  end

  def compute_version()
    if @version
      return
    end
    #find the directories that contain a p2 repository
    #sort them by name and use the last one for the actual last version.
    #increment that version.
    current_latest=compute_last_version @outputPath
    if current_latest.nil?
     # raise "Expecting to find a version number in #{@outputPath}"
      @version="1.0.0.000"
    else
      @currently_released_repo=compute_children_repos File.join(@outputPath,current_latest)
      @version=increment_version current_latest
    end
    #puts @version
  end

  #returns the last version folder
  #parent_dir contains version folders such as 1.0.0.001, 1.0.0.002 etc
  def compute_last_version(parent_dir, version_glob="*")
    puts "compute_last_version in #{parent_dir} using '#{version_glob}'"
    glob=File.join(parent_dir,version_glob)
    puts "Looking for the last version in #{glob}"
    versions = Dir.glob(File.join(glob,"artifacts.*")) | Dir.glob(File.join(glob,"compositeArtifacts.*"))
    sortedversions= Array.new
    versions.uniq.sort.each do |path|
      if FileTest.file?(path) && !FileTest.symlink?(File.dirname(path)) && "latest" != File.basename(File.dirname(path))
        aversion= File.basename File.dirname(path)
        sortedversions << aversion
      end
    end
    return sortedversions.last, sortedversions
  end

  def compute_versioned_output()
    compute_version
    @versionned_output_dir = "#{@outputPath}/#{@version}"
    if @test != "true"
      if File.exist? @versionned_output_dir
        puts "warning: removing the existing directory #{@versionned_output_dir}"
        FileUtils.rm_rf @versionned_output_dir
      end
      FileUtils.mkdir_p @versionned_output_dir
    end
  end

  # increment a version. if the version passed is 1.0.0.019, returns 1.0.0.020
  # keeps the padded zeros
  def increment_version(version)
    toks = version.split "."
    buildnb = toks.last
    incremented = buildnb.to_i+1
    inc_str_padded = "#{incremented.to_s.rjust(buildnb.size, "0")}"
    toks.pop
    toks.push inc_str_padded
    return toks.join "."
  end

  def compute_children_repos(compositeRepoFolder)
    children_repos = Array.new
    compositeArtifacts=File.join(compositeRepoFolder,"compositeArtifacts.xml")
    if !File.exist? compositeArtifacts
      puts "Warn #{compositeArtifacts} does not exists"
      return;
    end
    file = File.new(compositeArtifacts, "r")

    while (line = file.gets)
      #look for a line that contains <child location="../../be/3.0.0.178"/>
      #extract the location attribute.
      #put it in the array.
      m = /<child location="(.*)"\/>/.match line
      if m
        children_repos.push m[1]
        puts "found one in '#{m[1]}'"
      end
    end
    file.close
    children_repos.sort!
  end

  COMPOSITE_XML_RHTML = %(<?xml version="1.0" encoding="UTF-8"?>
<?composite<%=@ArtifactOrMetadata%>Repository version="1.0.0"?>
<repository name="&quot;<%=@name%>-<%=@version%>&quot;"
    type="org.eclipse.equinox.internal.p2.metadata.repository.Composite<%=@ArtifactOrMetadata%>Repository" version="1.0.0">
  <properties size="1">
    <property name="p2.timestamp" value="<%=@timestamp%>"/>
  </properties>
  <children size="<%=@children_repo.size%>">
    <%@children_repo.each do |child_repo| %><child location="<%=child_repo%>"/>
    <%end%>
  </children>
</repository>
)
  COMPOSITE_INDEX_XML_RHTML = %(<?xml version="1.0" encoding="UTF-8"?>
<html>
  <head><title>Composite Repository <%= @name %>-<%= @version %></title></head>
  <body>
    <p>This the P2-update site for the <%= @name %> features.</p>
    <p>The code can be found in the git repository <%= `git ls-remote --get-url` %></p>
    <h3>For more info see <a  href="http://download.elexis.info/">http://download.elexis.info/</a></h3>
    <h3>Stable releases</h3>
    <ol>
    <li>Elexis 3.0.0 was released on August 3 2014</li>
    </ol>
    <h3>Further information</h3>
    <p>The p2-update site service is sponsored by Medelexis AG. Thanks a lot!</p>
    <p>For questions and suggestions send an e-mail to the <a  href="mailto:elexis-develop@lists.sourceforge.net">elexis developer</a></p>
    <h3>Content of <%= @name %>-<%= @version %> built on <%= @date %></h3>
    <ul>
      <%@children_repo.each do |child_repo| %><li><a href="<%= child_repo %>"><%= child_repo %></a></li>
      <% end %>
    </ul>
    <p>Link to the actual sources of the composite repository:
    <ul>
      <li><a href="compositeArtifacts.xml">compositeArtifacts.xml</a></li>
      <li><a href="compositeArtifacts.xml">compositeContent.xml</a></li>
    </ul>
    </p>
  </body>
</html>
  )
  def emit
    current_dir=File.expand_path(File.dirname(__FILE__))
    #Generate the Artifact Repository
    composite_rhtml = File.join(current_dir,"composite.xml.rhtml")
    if File.exists?(composite_rhtml)
      template = ERB.new(File.new(composite_rhtml).read, nil, "%")
    else
      template = ERB.new(COMPOSITE_XML_RHTML, nil, "%")
    end
    artifactsRes=template.result(self.get_binding)

    #Generate the Metadata Repository
    self.set_ArtifactOrMetaData "Metadata"
    metadataRes=template.result(self.get_binding)

    #Generate the HTML page.
    composite__index_rhtml = File.join(current_dir,"composite_index_html.rhtml")
    if File.exists?(composite__index_rhtml)
      html_template = ERB.new(File.new(composite__index_rhtml).read, nil, "%")
    else
      html_template = ERB.new(COMPOSITE_INDEX_XML_RHTML, nil, '%')
    end
    htmlRes=html_template.result(self.get_binding)

    p2_index = "version = 1
metadata.repository.factory.order = compositeContent.xml,\!
artifact.repository.factory.order = compositeArtifacts.xml,\!
"

    if @test == "true"
      puts "=== compositeArtifacts.xml:"
      puts artifactsRes
      puts "=== compositeContent.xml:"
      puts metadataRes
      puts "=== index.html:"
      puts htmlRes
    elsif
      out_dir = @versionned_output_dir
      File.open(File.join(out_dir,"compositeArtifacts.xml"), 'w') {|f| f.puts(artifactsRes) }
      File.open(File.join(out_dir,"compositeContent.xml"), 'w') {|f| f.puts(metadataRes) }
      File.open(File.join(out_dir,"index.html"), 'w') {
        |f|
        f.puts(htmlRes)
      }
      File.open(File.join(out_dir,"p2.index"), 'w') {|f| f.puts(p2_index) }
      puts "Wrote the composite repository in #{out_dir}"
    end
  end
end

property_file = File.join(File.dirname(__FILE__), 'repo.properties')
ini_lines = IO.readlines(property_file)
puts "Read repo.properties from #{property_file}"
ini = {}
ini_lines.each{ |line| splitted = line.strip.split('='); ini[splitted[0]] = splitted[1] }
fullVersion = "#{ini['version']}.#{ini['qualifier']}"
repo_name = ini['repoName']
project = File.expand_path(File.join(__FILE__, '..', '..', ini['projectName']))
if ARGV.size == 1
	root = ARGV[0]
else
	ENV['ROOT'] ||= "/srv/www/download.elexis.info/#{repo_name}"
	root = ENV['ROOT']
end
destBaseDir = "#{root}/versions_4_#{ini['repoVariant']}/"
dest = File.expand_path("#{destBaseDir}/#{fullVersion}")
unless File.directory?(dest)
  FileUtils.makedirs(dest, :verbose => true)
  FileUtils.cp_r(Dir.glob("#{project}/target/repository/*"),  dest, :verbose => true)
  File.open(File.join(dest, "p2.index"), 'w') {|f| f.puts("version = 1
metadata.repository.factory.order = content.xml,\!
artifact.repository.factory.order = artifacts.xml,\!
") }

end

compositeRepository=CompositeRepository.new root, ini['repoVariant'], destBaseDir, ini['repoName'], 'otherurls'
compositeRepository.emit
FileUtils.cp(File.expand_path(__FILE__),compositeRepository.get_versionned_output_dir, :verbose => true) # copy myself to be able to be downloaded!
