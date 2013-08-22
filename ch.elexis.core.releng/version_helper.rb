#!/usr/bin/env ruby
# Copyright 2013 by Niklaus Giger <niklaus.giger@member.fsf.org>
#
# Some helper function for the Elexis 3 release process
# Allows patching the version of
# * pom.xml
# * MANIFEST.MF
# * feature.xml
# * category.xml
# * p2.inf
# * *.product

require "rexml/document"
include REXML  # so that we don't have to prefix everything with REXML::...

class VersionUpdater
  
  attr_reader = :filename, :eclipseVersion, :mavenVersion, :dryRun
  
  def initialize(filename, newVersion, dryRun = false)
    unless File.exists?(filename)
      puts "Could not read file #{filename} to update version to #{newVersion}"
      exit 1
    end
    # puts "Updating #{filename}"
    @mavenVersion   = newVersion.sub(/([\.\-_]qualifier)/, '-SNAPSHOT').sub('.-','-')
    @eclipseVersion = newVersion.sub('-SNAPSHOT', '.qualifier')
    @filename       = filename
    @newVersion     = newVersion
    @dryRun         = dryRun
  end

  def update_pom
    return if reportDryRun(@mavenVersion)
    doc = Document.new File.new (@filename)
    root = doc.root
    if /qualifier/.match(@mavenVersion) 
      puts "Illegal maven version #{@mavenVersion}. Defined using #{@newVersion}. eclipseVersion #{@eclipseVersion}"
      exit 2
    end
    root.elements["version"].text = @mavenVersion if root.elements["version"]
    root.elements["parent/version"].text = @mavenVersion if root.elements["parent/version"]
    writeXmlOutput(doc)
  end

  def update_mf
    return if reportDryRun(@eclipseVersion)
    content = IO.readlines(@filename)
    content.each {
      |line|
        if /Bundle-Version\:/.match(line)
          newLine = "Bundle-Version: #{@eclipseVersion}\n"
          next if newLine.eql?(line)
          puts "Patching #{@filename} #{line.chomp} -> #{newLine}"
          line.sub!(line, newLine)
          break
        end
    }
    writeOutput(content)
  end

  def update_feature
    return if reportDryRun(@eclipseVersion)
    doc = Document.new File.new (@filename)
    feature = doc.root
    if feature.attributes['label']
      label = feature.attributes['label']
      oldVers = /[\d\.-]+\w*(SNAPSHOT|qualifier|-|v)*/.match(label)
      unless oldVers[0].eql?(@eclipseVersion)
        puts "patching #{@filename}: #{oldVers[0]} -> #{newVersion}"
        feature.attributes['label'] = feature.attributes['label'].sub(oldVers[0], @eclipseVersion) if oldVers
      end if oldVers
    end
    feature.attributes['version'] = @eclipseVersion if feature.attributes['version']
    writeXmlOutput(doc)
  end

  def update_category
    return if reportDryRun(@eclipseVersion)
    doc = Document.new File.new (@filename)
    root = doc.root
    regExp = /feature_(\d.*).jar/
    oldVers = regExp.match(root.elements["feature"].attributes["url"])[1]
    return if oldVers.eql?(@eclipseVersion) and root.elements["feature"].attributes["version"].eql?(@eclipseVersion)
    root.elements["feature"].attributes["version"] = @eclipseVersion
    root.elements["feature"].attributes["url"].sub!(oldVers, @eclipseVersion)
    puts "Patching #{@filename}:  #{oldVers} -> #{@eclipseVersion}"
    writeXmlOutput(doc)
  end
  
  def update_product
    return if reportDryRun(@eclipseVersion)
    pp "update_product #{@filename}"
    doc = Document.new File.new (@filename)
    root = doc.root
    regExp = /feature_(\d.*).jar/
    if root.elements["features/feature"]
      root.each_element("//features/feature") { |x| pp x;  x.attributes["version"] = @eclipseVersion } 
      puts "Patching #{@filename}  -> #{@eclipseVersion}"
      writeXmlOutput(doc)
    end
  end
  
  # updates the file using the first match group of the regexp given.
  # Parses the file line by line
  def update_using_regexp(regExp, newVersion= @eclipseVersion)
    return if reportDryRun(newVersion)
    content = IO.readlines(@filename)
    content.each {
      |line|
        if match = regExp.match(line)
          newLine = line.sub(match[1], newVersion)          
          next if newLine.eql?(line)
          puts "Patching #{@filename} \n  #{line.chomp} ->\n  #{newLine}"
          line.sub!(line, newLine)
          break
        end
    }
    writeOutput(content)
  end
  
private
  def writeXmlOutput(doc)
    formatter = REXML::Formatters::Pretty.new
    formatter.compact = true # This is the magic line that does what you need!
    ausgabe = File.open("#{@filename}", 'w+')
    formatter.write(doc.root, ausgabe,)
  end

  def writeOutput(content)
    ausgabe = File.open("#{@filename}", 'w+')
    ausgabe.puts(content)
    ausgabe.close
  end
  
  def reportDryRun(newVersion)
    if @dryRun
      puts "Would patch #{@filename} -> #{newVersion}"
      return true
    end
    false
  end
end

