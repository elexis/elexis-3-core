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
# https://rubygems.org/gems/java-properties
# and https://rubygems.org/gems/java_properties or https://rubygems.org/gems/properties-ruby
# nix-shell -p bundler sqlite rubyPackages.do_sqlite3 --command fish

puts "It may take some time for bundler/inline to install the dependencies"
require 'bundler/inline'
require 'bundler'
Bundler.configure_gem_home_and_path "#{ENV['HOME']}/.cache"

gemfile do
  source 'https://rubygems.org'
  gem 'pry-byebug'
end

require 'csv'
require_relative 'common_l10n'
$stdout.sync = true

FIX_NAME_CSV = File.dirname(__FILE__) + '/fix_names.csv'
Fixes =  CSV.read(FIX_NAME_CSV)
BETTER_NAMES = {}
Fixes[1..-1].each { |x| BETTER_NAMES[x[1]] = x[2]}

L10N_KEYS=[]
def better_names_for_java_files(dir)
  files = Dir.glob(dir + '/**/*.java')
  puts "better_names #{dir} handling #{files.size}"
  files.each do | javafile |
	content = File.read(javafile)
    old_content = content.clone
    if File.basename('Messages.java')
        BETTER_NAMES.each{|old, better| content.gsub!(/(\sString\s+)#{old}(\W|$)/, '\1'+better+'\2')}
    end
    BETTER_NAMES.each{|old, better|content.gsub!(/(Messages.)#{old}(\W|$)/,  '\1'+better+'\2')}
    File.open(javafile, 'w') do |file|
        puts "Better names for Javafile  #{javafile}"
        file.write(content)
    end unless content.eql?(old_content)
  end
end
def better_names_for_properties(dir)
  files = Dir.glob(dir + '/**/messages*.properties')
  files.each do | propFile |
	content = File.read(propFile)
    old_content = content.clone
    BETTER_NAMES.each{|old, better|
                      begin
            content.gsub!(/^#{old} =/,  better + ' =')
                     rescue => error
                         puts "#{error} in #{propFile}"
                     end
                     }
    newLines = []
    keys = []
    found = false
    File.readlines(propFile).each_with_index do |line, idx|
        if found then found = false; next; end
            newLines << line
            begin
            if m = /(\w+) =/.match(line)
                found = true
                keys << m[1] unless keys.index(m[1])            
                L10N_KEYS << m[1] unless L10N_KEYS.index(m[1])
            end
                     rescue => error
                         puts "#{error} in #{propFile}"
                     end
    end                 
    File.open(File.basename(propFile)+'.txt', 'w+') {|f| f.puts keys.uniq.sort.join("\n")}
    puts "better_names #{propFile} has #{keys.size} keys L10N_KEYS #{L10N_KEYS.uniq.size} size"
    unless content.eql?(old_content)
        File.open(propFile, 'w') do |file|
            file.write(content)
        end
        File.open(propFile, 'w') do |file|
            file.write(newLines.join(''))
        end
    end
  end
end
                      
def handle_l10n_messages
   javafile = Dir.glob("**/src/ch/elexis/core/l10n/Messages.java").first
    if javafile
       keys = get_keys_from_messages_java(javafile)
       all_keys = (L10N_KEYS +  keys).uniq.sort
       binding.pry
       all_keys.uniq!
        puts "better_names #{javafile} has #{all_keys.size} keys L10N_KEYS #{L10N_KEYS.uniq.size} size #{keys.size} size"
       File.open('l10n_java.txt', 'w+') {|f| f.puts all_keys.join("\n")}
       emit_l10_messages_java(javafile, all_keys)
    end
end

start_time = Time.now
ARGV.each{|x| puts x; better_names_for_java_files(x); better_names_for_properties(x); handle_l10n_messages }
end_time = Time.now
puts "Done in #{(end_time - start_time).to_i} seconds at #{end_time}"
