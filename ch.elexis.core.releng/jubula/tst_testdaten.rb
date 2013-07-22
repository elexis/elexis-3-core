#!/usr/bin/env ruby
# encoding: UTF-8
# http://xml-simple.rubyforge.org/
# http://stackoverflow.com/questions/10300095/how-to-add-child-nodes-in-nodeset-using-nokogiri
require 'pp'
require 'nokogiri'
require 'ostruct'
require 'xmlsimple'

name = "ElexisCore_1.0.xml"
f = File.open(name)
doc = Nokogiri::XML(f)
node_set = Nokogiri::XML::NodeSet.new(doc)
doc = Nokogiri::Slop(f)
ref = XmlSimple.xml_in(name)

#elem = {'rowCount' => 1, [ 'data' => [{"language"=>["de_DE"], "value"=>["Abrechnungssysteme"]} ] } ] }
elem = {"rowCount"=>"1",
 "data"=>
  [{"columnCount"=>"1",
    "data"=>[ {"language"=>["de_DE"], "value"=>["Abrechnungssysteme"] } ]
   }]
}

pp elem

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
require File.join(File.dirname(__FILE__), 'jubula_plugin_info.rb')
pageNames = EclipseJar.getTranslatedPreferencePages
counter = 0
pages = []
pageNames.each{|name|
  counter += 1
  pages << eval(genPreferencePageElement(counter, name))
  }

j=0
ref['project'].first['namedTestData'].each{ 
  |td|
j += 1
    next unless /VIEWS|PERSPECTIVES/i.match(td['name'])
pp j
    pp td['testData'][0]
    td['testData'][0]['row'] = pages
}
exit
j=0
ref['project'].first['namedTestData'].each{ 
  |td|
j += 1
pp j
    pp td
    next unless /PREFERENCES/i.match(td['name'])
    puts 99999
    pp td['testData'][0]
    td['testData'][0]['row'] = pages
    break
}
pp 888

changed = File.open(name.sub('.xml','.changed'), 'w+')
changed.puts '<?xml version="1.0" encoding="UTF-8"?>'
changed.write XmlSimple.xml_out(ref).sub('<opt xmlns','<content xmlns').sub('</opt>','</content>')
changed.close

