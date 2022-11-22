#!/usr/bin/env ruby
# Copyright 2017 by Niklaus Giger <niklaus.giger@member.fsf.org>
#

L10N_Cache_Entry = Struct.new('L10N_Cache_Entry', :key, :java, :de, :en, :fr, :it)
KEYS = [:java, :de, :en, :fr, :it]
RBE_FILE_OPTIONS_FOR_WRITE = 'w+:ASCII'
KEY_REGEX_IN_MESSAGES = /String\s+(\w+)(;|.=.*)/
KEY_REGEX_MATCH_L10N_MESSAGE = /String\s+\w+\s*=\s*ch\.elexis.core\.l10n\.Messages\.(\w+)/
REGEX_MESSAGES_REF = /.*Messages.(\w+)[\s;$]/
OLD_MESSAGES_HEADER = 'package ch.elexis.core.l10n;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
       // BUNDLE_NAME is needed for core.data
       public static final String BUNDLE_NAME = "ch.elexis.core.l10n.messages";
'
  OLD_TAIL = '       static { // load message values from bundle file
               NLS.initializeMessages(BUNDLE_NAME, Messages.class);
       }'
  NEW_MESSAGES_HEADER = 'package ch.elexis.core.l10n;
import org.eclipse.e4.core.services.nls.Message;
public class Messages {
	   // BUNDLE_NAME is neede for a core.data	   -import org.eclipse.osgi.util.NLS;
public static final String BUNDLE_NAME = "ch.elexis.core.l10n.messages";

'
def to_utf(string)
    begin
        /String\s+(\w+)\s*;/.match(string)
    rescue
        string = string.encode('UTF-8', 'ISO-8859-1')
    end
    string
end

def get_keys_from_messages_java(msg_java)
    lines = File.readlines(msg_java).collect{|line| to_utf(line) }
    keys = lines.collect{|line| m = KEY_REGEX_IN_MESSAGES.match(line); m[1] if m && !m[1].eql?('BUNDLE_NAME') }.compact
    puts "get_keys_from_messages_java #{msg_java} has #{keys.size} keys" if $VERBOSE
    keys
end
def emit_l10_messages_java(msg_java, keys)
    index = 0
    File.open(msg_java, 'w') do |content|
        content.puts(OLD_MESSAGES_HEADER)
        keys.each do |key|
        content.puts "	public static String #{key};"
        end
        content.puts OLD_TAIL
        content.puts('}');
        puts "Wrote #{keys.size} Java variables to #{msg_java}"
    end
end


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
