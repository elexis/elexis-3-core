#!/usr/bin/env ruby
# Copyright 2017 by Niklaus Giger <niklaus.giger@member.fsf.org>
#

L10N_Cache_Entry = Struct.new('L10N_Cache_Entry', :key, :java, :de, :en, :fr, :it)
KEYS = [:java, :de, :en, :fr, :it]
RBE_FILE_OPTIONS_FOR_WRITE = 'w+:ASCII'

# Emit a line compatible with the Essiembre Ressource Bunde Editor of Eclipse Neon
def emit_RBE_compatible_line(file, tag_name, lang_value, is_plugin = false)
  @last_category ||= '-'
  begin
	line = "#{tag_name} = " + lang_value.dump.gsub(/^"|"$/,'').gsub('\\\\','\\').gsub('\\"', '"').gsub('\t', '\u0009')
	if is_plugin
	  category = tag_name.split('.')[0..-2].join('.')
	  unless category.eql?(@last_category)
		@last_category = category
	  end
	end
	file.puts "\n" + line
  rescue => error
	puts "#{error}: Could not write #{tag_name} #{lang_value}"
#	puts "#{caller.join("\n")}"
  end
end
