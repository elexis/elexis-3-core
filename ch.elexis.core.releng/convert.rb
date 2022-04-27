#!/usr/bin/env ruby
# Copyright 2017 by Niklaus Giger <niklaus.giger@member.fsf.org>
#
# TODO: Use google-translate
# https://github.com/GoogleCloudPlatform/ruby-docs-samples/tree/master/translate
#   * Need https://developers.google.com/accounts/docs/application-default-credentials
#   * gem install googleauth
#   * gem install google-api-client
# require 'google/apis/translate_v2'
# translate = Google::Apis::TranslateV2::TranslateService.new
# translate.key = 'YOUR_API_KEY_HERE'
# result = translate.list_translations('Hello world!', 'es', source: :en)
# puts result.translations.first.translated_text
# to connect to the database
#
# Use https://github.com/mcls/yaml_to_csv ??
# https://rubygems.org/gems/java-properties
# and https://rubygems.org/gems/java_properties or https://rubygems.org/gems/properties-ruby

puts "It may take some time for bundler/inline to install the dependencies"
require 'bundler/inline'

gemfile do
  source 'https://rubygems.org'
  gem 'optimist'
  gem "java-properties"
  gem 'pry-byebug'
end

require 'pry-byebug'
require 'csv'
require 'optimist'
require 'logger'

parser = Optimist::Parser.new do
  version "#{File.basename(__FILE__, '.rb')} (c) 2022 by Niklaus Giger <niklaus.giger@member.fsf.org>"
  banner <<-EOS
#{version}
License: Eclipse Public License 1.0 (EPL)
Useage: #{File.basename(__FILE__)} path_to_file
  Convert between csv and java properties for bundles/ch.elexis.core.l10n/src/ch/elexis/core/l10n/messages.csv | messages*.properties
  destination will be in the same directory of the path_to_file
  # DocHandle_importErrorDirectoryText
EOS
  opt :to_csv   ,         "Create csv for all languages with entries for all #{@messages}", :default => false, :short => '-c'
end

Options = Optimist::with_standard_exception_handling parser do
  raise Optimist::HelpNeeded if ARGV.empty? # show help screen
  parser.parse ARGV
end

srcFile = ARGV.first

require_relative 'common_l10n'

def handle_messages()
  search="./**/bundles/ch.elexis.core.l10n/src/ch/elexis/core/l10n/message*.properties"
  files = Dir.glob(search)
  unless files.size >0
	puts "Could not find message via #{search}"
	exit 1
  end
  languages = []
  contents = {}
  files.each do |file|
	lang = File.basename(file).sub('messages', '').sub('.properties', '').sub('_','')
	properties = JavaProperties.load(file)
	lang =  :java if lang.size == 0
	languages << lang
	properties.each do | key, value |
	  contents[key] ||= L10N_Cache_Entry.new(key, '', '', '', '', '')
	  contents[key][lang.to_sym] = value
	end
  end
  headers = [:key, :java, :de, :en, :fr, :it]
  destFile = File.dirname(files.first) + '/messages.csv'
  CSV.open(destFile, "w") do |csv|
	csv << headers
	contents.keys.each do |key|
	  csv << contents[key]
	end
  end
  puts "Wrote #{contents.size} entries to #{destFile}"
end

def handle_csv_source(srcFile)
  destFile = srcFile.sub(File.extname(srcFile),'.properties')  
  puts "Converting #{srcFile} to #{destFile}"
  array = CSV.read(srcFile)
  KEYS.each do |lang|
	lang_file = destFile.sub('.properties', (lang.eql?(:java) ? '' : '_' + lang.to_s) + '.properties')
	File.open(lang_file, RBE_FILE_OPTIONS_FOR_WRITE) do |file|
	  array.each do | entry |
		tag2write = entry.first
		next if /L10N_Cache_Entry/.match(tag2write)
		lang_value = entry[KEYS.index(lang)+1].to_s
		emit_RBE_compatible_line(file, tag2write, lang_value, true)
	  end
	end
  end
end

handle_messages if Options[:to_csv]
handle_csv_source(srcFile) if srcFile && File.exist?(srcFile) && /.csv/.match(srcFile)


