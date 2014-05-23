#!/usr/bin/env ruby
# encoding: UTF-8

require 'pp'
# s.a. http://javathreads.de/2008/10/die-wichtigsten-utf-8unicode-sonderzeichen-fuer-die-entwicklung-mit-facelets/
unless /^(1\.9|2)/.match(RUBY_VERSION)
  puts "Must be run with RUBY >= 1.9, is #{RUBY_VERSION}"
  exit
end
require 'zip/zip'
require "rexml/document"
include REXML  # so that we don't have to prefix everything with REXML::...

module EclipseHelpers
  # Some helpers, as eclipse properties file have non-ASCII characters escaped using UTF-8
  ConvTable = Hash.new
  ConvTable['\\u00A0'] = ' '
  ConvTable['\\u0026'] = '&'
  ConvTable['\\u003C'] = '<'
  ConvTable['\\u003E'] = '>'
  ConvTable['\\u00E4'] = 'ä'
  ConvTable['\\u00C4'] = 'Ä'
  ConvTable['\\u00F6'] = 'ö'
  ConvTable['\\u00D6'] = 'Ö'
  ConvTable['\\u00FC'] = 'ü'
  ConvTable['\\u00DC'] = 'Ü'
  ConvTable['\\u00df'] = 'ß'
  ConvTable['\\u20AC'] = '€'
  ConvTable['\\u0024'] = '$'
  ConvTable['\\u00A3'] = '£'

  def EclipseHelpers::my_unescape(inhalt)
  ConvTable.each{
    |old, new|
      begin
        inhalt.gsub!(old, new)
      rescue => e
        puts e
        puts "Tried #{old} -> #{new}"
      end
    }
    inhalt.clone
  end
end

class EclipseJar
  # Some helper classes for the extension points we are interested in
  UI_PreferencePage = Struct.new('UI_PreferencePage', :id, :category, :translation)
  UI_View           = Struct.new('UI_View',           :id, :category, :translation)
  UI_Perspective    = Struct.new('UI_Perspective',    :id, :category, :translation)
  Category          = Struct.new('Category',          :id, :name, :category, :translation) # we can have nested categories
  @@views                     = Hash.new
  @@view_categories           = Hash.new
  @@preferencePages           = Hash.new
  @@perspectives              = Hash.new
  @@prefPage_categories       = Hash.new
  @@view_categories           = Hash.new
  attr_reader :views, :view_categories, :preferencePages, :perspectives
  Dest = File.join(File.dirname(__FILE__), 'plugin_infos');
  FileUtils.makedirs(Dest)
  
  def initialize(jarname, iso='de')
    @iso                       = iso
    @jarname                   = jarname
    @jarfile                   = Zip::ZipFile.open(jarname) 
    # we use hashes to be able to find the categories fast
    if @jarfile.find_entry('plugin.xml')
      out = File.open(File.join(Dest, File.basename(jarname).sub('.jar','.xml')), 'w+')
      out.write(@jarfile.read('plugin.xml'))
    end if $VERBOSE # handy for analysing all plugin.xml
    readPluginXML(File.basename(jarname))
  rescue # HACK: we need this to handle org.apache.commons.lang under Windows-7
    puts "Skipped plugin #{File.expand_path(jarname)}"
  end
  
  def addCategory(hash, id, name = nil, category = nil)
    return if hash[id] and hash[id].translation
    hash[id] = Category.new(id, name, category) unless hash[id]
    translation = getTranslationForPlugin(name, @iso) if name
    hash[id].translation = translation if name and translation
    puts "#{File.basename(@jarname)}: Added category #{id} name #{name} tr '#{translation}'" if $VERBOSE
  end
  
  def EclipseJar.getTranslatedPreferencePages
    all = []
    # We can have nested categories like Textverarbeitung/Schablonenprozessor/OpenOffice.org Processor
    @@preferencePages.each{
      |id, content|
        iterator  = @@prefPage_categories[content.category]
        text = content.translation
        while iterator
          text = "#{iterator.translation}/#{text}"
          iterator = @@preferencePages[iterator.category]
        end
        all << text
    }
    all.sort.reverse.uniq if all and all.size > 0
  end
  
  def EclipseJar.getTranslatedViews
    all = []
    @@views.each{
      |id, content| 
        category =  content.category
        cat_trans = content.translation
        text = nil
        if category
          text = "#{@@view_categories[category].translation}/#{content.translation}"
        else
          text = "Other/#{content.translation}"
        end
        all << text if text
    }
    all.sort.reverse.uniq if all and all.size > 0
  end
  def EclipseJar.getTranslatedPerspectives
    all = []
    @@perspectives.each{
      |id, content| 
        category =  content.category
        cat_trans = content.translation
        text = nil
        if category
          text = "#{@@perspectives[category].translation}/#{content.translation}"
          puts "perspectives #{id} category #{category.inspect} text #{cat_trans}" if $VERBOSE
        else
          text = content.translation
          puts "perspectives #{id} categories #{category} text #{text}" if $VERBOSE
        end
        all << text
    }
    all.sort.reverse.uniq if all and all.size > 0
  end
  
  def EclipseJar::views
    @@views
  end
       
  def EclipseJar::view_categories
    @@view_categories
  end
       
  def EclipseJar::perspectives
    @@perspectives
  end
       
  def EclipseJar::preferencePages
    @@preferencePages
  end
       
  def getTranslationForPlugin(look_for, iso)
    properties = "plugin_#{iso}.properties"
    properties = "plugin.properties" unless @jarfile.find_entry(properties)
    puts "Looking for translation of #{look_for} in #{properties}"  if $VERBOSE
    line_nr = 0
    @jarfile.read(properties).split("\n").each {
      |line|
          line_nr += 1
          id,value = line.split(' = ')
          if id and id.index(look_for) and value
            return EclipseHelpers::my_unescape(value.sub("\r","").sub("\n",""))
          else id,value = line.split('=')
            return EclipseHelpers::my_unescape(value.sub("\r","").sub("\n","")) if id and id.index(look_for)
          end
    } if @jarfile.find_entry(properties)
    return look_for # default
  end

  def readPluginXML(plugin_xml)
    return unless  @jarfile.find_entry('plugin.xml')
    doc = Document.new @jarfile.read('plugin.xml')
    # Get all perspectives
    root = doc.root
    res = []
    root.elements.collect { |x| res << x if /org.eclipse.ui.perspectives/.match(x.attributes['point']) }
    res[0].elements.each{
      |x|
      id = x.attributes['name'].sub(/^%/,'')
      @@perspectives[id] = UI_Perspective.new(id, nil, getTranslationForPlugin(id, @iso))
    } if res and res[0] and res[0].elements
    puts "found #{@@perspectives.size} perspectives in #{plugin_xml}" if $VERBOSE

    # Get all views
    res = []
    root.elements.collect { |x| res << x if /org.eclipse.ui.views/.match(x.attributes['point']) }
    res[0].elements.each{
      |x|
      name     = x.attributes['name'].sub(/^%/,'') if  x.attributes['name']
      id       = x.attributes['id'].sub(/^%/,'')
      if x.name.eql?('category')
        addCategory(@@view_categories, id, name)
      elsif x.attributes['name']
        category = x.attributes['category']
        translation =  getTranslationForPlugin(name, @iso)
        puts "#{File.basename(@jarname, '.jar')}: Adding view: id #{id} category #{category.inspect} translation #{translation}" if $VERBOSE
        unless category
          @@views[id]           = UI_View.new(id, nil, translation)
        else
          @@views[id]           = UI_View.new(id, category, translation)
        end
      end
    } if res and res[0] and res[0].elements
    puts "found #{@@views.size} views and #{@@view_categories.size} categories" if $VERBOSE
    
     # Get all preferencePages
    res = []
    root.elements.collect { |x| res << x if /org.eclipse.ui.preferencePages/.match(x.attributes['point']) }
    res[0].elements.each{
      |x|
      name     = x.attributes['name'].sub(/^%/,'')
      id       = x.attributes['id'].sub(/^%/,'')
      category = x.attributes['category']
      addCategory(@@prefPage_categories, id, name, category) unless category
      translation =  getTranslationForPlugin(name, @iso)
      puts "Adding preferences: id #{id} category #{category.inspect} translation #{translation}" if $VERBOSE
      unless category
        @@preferencePages[id]           = UI_PreferencePage.new(id, nil, translation)
      else
        @@preferencePages[id]           = UI_PreferencePage.new(id, category, translation)
      end
      @@prefPage_categories[id] = @@preferencePages[id] unless @@prefPage_categories[id] # for nested categories
    } if res and res[0] and res[0].elements
    puts "#{sprintf("%-40s", File.basename(File.dirname(plugin_xml)))}: now #{@@preferencePages.size} preferencePages" if $VERBOSE
  end
  
  def EclipseJar.parsePluginDir(plugins_dir =  "/opt/elexis-3-core/ch.elexis.core.releng/product/target/products/ch.elexis.core.application.product/win32/win32/x86_64/plugins")
    name = "#{plugins_dir}/*.jar"
    Dir.glob(name).each{
      |jarname|
        puts "Adding: #{jarname}" if $VERBOSE
        EclipseJar.new(jarname)
    }
    plugins_dir
  end
end

