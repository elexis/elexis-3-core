#!/usr/bin/env ruby
# encoding: UTF-8
#http://stackoverflow.com/questions/10300095/how-to-add-child-nodes-in-nodeset-using-nokogiri
require 'pp'
require 'nokogiri'
require 'ostruct'

xml = <<-'XML'
<Catalog>
  <Interface></Interface>
  <Dialog></Dialog>
  <Manifest></Manifest>
</Catalog>
XML

collection = [
  OpenStruct.new(book: '1984', pen: 'George Orwell'),
  OpenStruct.new(book: 'Thinking, Fash and Slow', pen: 'Daniel Kahneman')
]

doc = Nokogiri::XML(xml) 
catalog = doc.root

node_set = Nokogiri::XML::NodeSet.new(doc)
collection.each do |object|
  book = Nokogiri::XML::Node.new('Book', doc)
  book_author = Nokogiri::XML::Node.new('Book_Author', doc)

  book.content = object.book
  book_author.content = object.pen

  node_set << book
  node_set << book_author
end

catalog.first_element_child.before(node_set)

# http://xml-simple.rubyforge.org/
# puts doc.to_xml
require 'xmlsimple'
name = "TestDaten.xml"
name = "ElexisCore_1.0.xml"
f = File.open(name)
doc = Nokogiri::XML(f)
node_set = Nokogiri::XML::NodeSet.new(doc)
doc = Nokogiri::Slop(f)
ref = XmlSimple.xml_in(name, { 'KeepRoot' => true })
ref = XmlSimple.xml_in(name)
ausgabe = File.open(name.sub('.xml','.unchanged'), 'w+')
ausgabe.puts '<?xml version="1.0" encoding="UTF-8"?>'
ausgabe.write(XmlSimple.xml_out(ref))
ausgabe.close
#puts pp ref['project']
#puts ref['project'].class#
#pp ref['project'].first['namedTestData']
pp ref['project'].first['namedTestData'].first['name']
pp ref['project'].first['namedTestData'][0]['testData'][0]['row'].size
pp ref['project'].first['namedTestData'][0]['testData'][0]['row'][0]

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
pageNames = EclipseJar.getTranslatedPrefencePages
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

