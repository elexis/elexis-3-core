#!/usr/bin/env ruby
# encoding: UTF-8
#
# A helper to patch the dataset of a Jubula Test-XML. See help
#

# We decided to use xml-simple, as I found nokogiri a bit more difficult to generate XML-elements
# http://xml-simple.rubyforge.org/
# http://stackoverflow.com/questions/10300095/how-to-add-child-nodes-in-nodeset-using-nokogiri

require 'pp'
require 'ostruct'
require 'xmlsimple'
require 'optparse'

require File.join(File.dirname(__FILE__), 'jubula_plugin_info.rb')

elem = {"rowCount"=>"1",
 "data"=>
  [{"columnCount"=>"1",
    "data"=>[ {"language"=>["de_DE"], "value"=>["Abrechnungssysteme"] } ]
   }]
}

def genPreferencePageElement(rowCount = 1, name='dummy', language= 'de_DE')
  s = %(
{"rowCount"=>"#{rowCount}",
  "data"=>
    [{"columnCount"=>"1",
      "data"=>[ {"language"=>["#{language}"], "value"=>["#{name}"] } ]
    }]
}
  )
  s
end

def genScreenshotElement(rowCount = 1, name='dummy', prefix = '', language= 'de_DE')
  s = %(
{"rowCount"=>"#{rowCount}",
  "data"=>
    [{"columnCount"=>"1",
      "data"=>[{"language"=>["#{language}"], "value"=>["#{name}.*"]}]},
    {"columnCount"=>"2",
      "data"=>
      [{"language"=>["#{language}"], "value"=>["screenshots/#{prefix}#{name}.png"] }]
    }]
}
    )
  s
end

def patchJubulaXML(xml_name, plugin_dir = nil)
  xml_name ||= "ElexisCore_1.0.xml"
  unless File.exists?(xml_name)
    puts "Could not find file #{xml_name}"
    exit 1
  end
  f = File.open(xml_name)
  doc = Nokogiri::XML(f)
  node_set = Nokogiri::XML::NodeSet.new(doc)
  doc = Nokogiri::Slop(f)
  ref = XmlSimple.xml_in(xml_name)

  plugin_dir ? EclipseJar.parsePluginDir(plugin_dir) : plugin_dir = EclipseJar.parsePluginDir()
  pageNames         = EclipseJar.getTranslatedPreferencePages
  viewNames         = EclipseJar.getTranslatedViews
  perspectiveNames  = EclipseJar.getTranslatedPerspectives
  unless pageNames and viewNames and perspectiveNames
    puts "Could not find view, preference pages or perspectives in #{plugin_dir}"
    exit
  end
  counter = 0; pages = []
  pageNames.each{|name|
    counter += 1
    pages << eval(genPreferencePageElement(counter, name))
  }

  counter = 0; views = []
  viewNames.each{|name|
    counter += 1
    views << eval(genScreenshotElement(counter, name))
  }

  counter = 0; perspectives = []
  perspectiveNames.each{|name|
    counter += 1
    perspectives << eval(genScreenshotElement(counter, name, 'p_'))
  }

  counter=0
  ref['project'].first['namedTestData'].each{ 
    |td|
  counter += 1
  case td['name']
    when 'PREFERENCES'
      puts "Changing #{pages.size} pages" if $VERBOSE
      td['testData'][0]['row'] = pages
    when 'VIEWS'
      puts "Changing #{views.size} views" if $VERBOSE
      td['testData'][0]['row'] = views
    when 'PERSPECTIVES'
      puts "Changing #{perspectives.size} perspectives" if $VERBOSE
      td['testData'][0]['row'] = perspectives
    else
  end
  }

  changed = File.open(xml_name, 'w+')
  changed.puts '<?xml version="1.0" encoding="UTF-8"?>'
  changed.write XmlSimple.xml_out(ref).sub('<opt xmlns','<content xmlns').sub('</opt>','</content>')
  changed.close
  puts "Wrote changed xmlfile #{xml_name} used #{plugin_dir}"
end

@xml = nil
@plugins_dir = nil
options = OptionParser.new do |opts|
      opts.banner = %(Usage: #{File.basename($0)} [@options]
  Patches a Jubula XML file like this
    * Collect info from all jar file inside the plugin directory (jubula_plugin_info)
    * then the following changes are performed
    ** DataSet PERSPECTIVES is replaced with all perspectives found
    ** DataSet VIEWS is replaced with all view names found
    ** DataSet PREFERENCES is replaced with all preferences pages found
    )
      opts.on("--xml xml_file", "XML-File to patch") do |v|
  @xml = v
      end
      opts.on("--plugins plugin_dir", "Plugins directory of the installed eclipse application") do |v|
  @plugins_dir = v
      end
      opts.on("-h", "--help", "Show this help") do |v|
  puts opts
  exit
      end
end
patchJubulaXML(@xml, @plugins_dir)
