#!/usr/bin/env ruby
# coding: utf-8
# License: Eclipse Public License 1.0
# Copyright Niklaus Giger, 2012, niklaus.giger@member.fsf.org
require 'fileutils'

def patchJubulaDbXmlFile(fileName)
  name2 = fileName+".bkp"
  inhalt = IO.readlines(fileName)
  if inhalt[0].index('encoding="UTF-8"')
    puts "File #{fileName} already UTF-8" if $VERBOSE
    FileUtils.cp(fileName, name2, :verbose => false)
  else
    cmd = "iconv -f UTF-16 -t UTF-8 #{fileName}> #{name2}"
    system(cmd)
  end

  cmd = "xmllint #{fileName} --format --output #{fileName}"
  exit unless system(cmd)
end

ARGV.each{
  |fName|
  if !File.writable?(fName)
    puts "Cannot write file #{fName}"
  end
  patchJubulaDbXmlFile(fName)
}
