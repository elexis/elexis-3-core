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
  gem 'optimist'
  gem 'googleauth'
  gem 'google-api-client'
  gem 'google-cloud-translate'
  gem 'pry-byebug'
  gem "java-properties"
  gem "sqlite3"
  gem 'diff-lcs'
end

require_relative 'common_l10n'
require 'google/apis/translate_v2'
require "rexml/document"
include REXML  # so that we don't have to prefix everything with REXML::...
require 'ostruct'
require 'pp'
require 'uri'
require 'csv'
require 'net/http'
require 'json'
require 'optimist'
require 'logger'
require "sqlite3"
$stdout.sync = true

ELEXIS_BASE = File.expand_path(File.dirname(File.dirname(__FILE__)))
L10N_MESSAGES = Dir.glob("#{ELEXIS_BASE}/**/l10n/Messages.java").first
DB_NAME = defined?(RSpec) ? File.expand_path( "rsc/test_translations.db") : File.expand_path( "translations.DB")
DB = SQLite3::Database.new DB_NAME
DB.execute("PRAGMA case_sensitive_like=ON;")
# Create a table
rows = DB.execute <<-SQL
  create table if not exists translations (
	key varchar,
	java varchar,
	de varchar,
	en varchar,
	fr varchar,
	it varchar,
	occurences int
  );
SQL

def create_t_uses
rows2 = DB.execute <<-SQL
  create table if not exists t_uses (
	key varchar,
	file varchar,
	line integer
  );
SQL
end

def update_uses
  DB.execute "drop table if exists t_uses;"
  create_t_uses
  files = Dir.glob('**/*.java')
  puts files.size
  DB.transaction
  files.each do | file |
	lines = File.readlines(file)
#	puts "Analysing #{lines.size} lines for #{file}"
	lines.each_with_index do | line,index |
	  begin
		next unless m = /.*Messages.(\w+)/.match(line)
		DB.execute "insert into t_uses values ( ?, ? , ? )",  file, m[1], index
	  rescue => error
#		puts error; binding.pry
		0
	  end
	end

  end
  puts "Done with #{files.size} files"
  DB.commit
end

if false # some tests
  DB.execute "insert into translations values ( 'key', 'myJava', 'myDe', 'meEn', 'myFr', 'myIt')"
  DB.execute( "select * from translations" ) { |row| p row }
  DB.execute( "delete from translations" )
  DB.execute( "select * from translations" ) { |row| p row }
  DB.execute( "select * from t_uses" ) { |row| p row }
end

class GoogleTranslation
  @@updated_cache = false
  if defined?(RSpec)
    CacheFileCSV = File.join(Dir.pwd, 'rsc/test_google_translation_cache.csv')
    FileUtils.rm_f(CacheFileCSV, :verbose => true)
  else
    CacheFileCSV = File.join(Dir.home, 'google_translation_cache.csv')
  end
  puts "CacheFileCSV is #{CacheFileCSV}"
  BaseLanguageURL='https://translate.googleapis.com/translate_a/single?client=gtx&sl='
  Translate = Google::Apis::TranslateV2::TranslateService.new
  Translate.key = ENV['TRANSLATE_API_KEY']
    # url = "#{BaseLanguageURL}#{source_language}&format=json&tl=#{target_language}&dt=t&q=#{URI.encode(what)}"
    # uri = URI(url)
    # response = Net::HTTP.get(uri)
    # JSON.parse(response)

  def self.translationCache
    @@translationCache
  end
  def self.translate_text(what, target_language=:it, source_language=:de)
    key = [what, target_language, source_language]
    value = @@translationCache.find{|x, y| x[0].eql?(what) && x[1].eql?(target_language) && x[2].eql?(source_language) }
    unless value
      begin
        value = Translate.list_translations(what, target_language, source: source_language)
        @@translationCache[key] = value.translations.collect{|x| x.translated_text}
        puts "Added #{key} #{@@translationCache[key]}" if $VERBOSE
        @@updated_cache = true
      rescue => error
        puts error
        puts "translate_text failed. Is environment variable TRANSLATE_API_KEY not specified?"
        return
      end
    end
    value = @@translationCache[key]
    puts "TRANSLATED_KEY #{key} into #{value} #{value.first.encoding}" if $VERBOSE
    value = value.first if value.is_a?(Array)
  end
  def self.load_cache
    @@translationCache = {}
    if File.exist?(CacheFileCSV)
      CSV.foreach(CacheFileCSV, :encoding => 'utf-8', :force_quotes => true) do |cells|
        next if cells[0].eql?('src')
        key = [cells[0], cells[1], cells[2]]
        value = cells[3] ? cells[3].chomp : ''
        @@translationCache[key] = [value]
      end
    end
  end

  def self.save_cache
    return unless @@updated_cache
    puts "GoogleTranslation: Saving #{@@translationCache.size} entries to #{CacheFileCSV}"
    CSV.open(CacheFileCSV, "wb:UTF-8", :force_quotes => true) do |csv|
      csv << ['what', 'dst', 'src', 'translated']
      @@translationCache.each do |key, value|
        csv << [key[0],
                key[1],
                key[2],
                value].flatten
      end
    end
  end
  # Initialization
  GoogleTranslation.load_cache
  at_exit do
	GoogleTranslation.save_cache
  end
end

class L10N_Cache_Entry
  def isMsg?
	result = !!/#{TAG_SEPARATOR}/.match(self[:key])
  end
end

class L10N_Cache
  # We properties from plugin.xml/properties have an tag starting with
  # their project-id, then TAG_SEPARATOR, then the id
  TAG_SEPARATOR = ':'

  CSV_HEADER_START = ['translation_key']
  CSV_HEADER_SIZE  = L10N_Cache::CSV_HEADER_START.size
  JavaLanguage = :java
  LANGUAGE_KEYS = [ :de, :en, :fr, :it]

  TRANSLATIONS_CSV_NAME = 'translations.csv'
  Translations = Struct.new(:lang, :values)
  REGEX_TRAILING_LANG = /\.plugin$|\.(#{LANGUAGE_KEYS.join('|')})$/
  KEY_REGEX_IN_MESSAGES = /String\s+(\w+)(\s*|.=.*)/
  EscapeBackslash = /\\([\\]+)/

  @@hasChanges = false

  # Converts escapces like \u00 to UTF-8 and removes all duplicated backslash.
  # Verifiy it using the following SQL scripts
  # select * from db_texts where translation like '%\u00%';
  # select * from db_texts where translation like '%\\%';
  def self.convert_to_real_utf(string)
    string = string.gsub(EscapeBackslash, '\\')
    return string unless  /\\u00|/.match(string)
    strings = []
    begin
      string.split('"').each do |part|
        idx = 0
        while idx <= 5 && /\\u00/.match(part)
          part = eval(String.new('"'+part.chomp('\\')+'"'))
          idx += 1
        end
        strings << part
      end
    rescue => error
      puts error
    end
    res = strings.join('"')
    res += '"' if  /"$/.match(string)
    res
  end

  def self.get_translation(key, lang)
	entry = db_get_entry(key)
	return entry ? entry[lang] : ''
	return db_get_entry(key)
	entries = []
	entry = nil
	DB.execute("select * from translations where #{lang.to_s} = '#{text}' ") do
	  |row| 
	  entry = L10N_Cache_Entry.new(row.first, 
	  row[1], row[2], row[3], row[4], row[5])
	  entries << entry
	end
  end

  def self.db_get_entry(key)
	entry =  L10N_Cache_Entry.new
	entry[:key] = key
	DB.execute( "select * from translations where key = '#{key}' " ) do
	  |row|
	  entry = L10N_Cache_Entry.new(row.first, 
	  row[1], row[2], row[3], row[4], row[5])
	end
	entry
  end

  def self.search_text(text, lang)
  	entry =  L10N_Cache_Entry.new
	entry[lang.to_sym] = text
	entries = []
    text = '' unless text
	DB.execute( "select * from translations where #{lang.to_s} like '#{text.gsub("'", "_")}' " ) do
	  |row|
	  entry = L10N_Cache_Entry.new(row.first, 
	  row[1], row[2], row[3], row[4], row[5])
	  entries << entry
	end
	entries
  end
  
  def self.set_translation(key, lang, value)
    value = value ? self.convert_to_real_utf(value.sub('\\ u00', '\\u00')) : ''
	binding.pry if value.eql?('false')
	entry = db_get_entry(key)
	entry[lang] = value
	self.db_insert_or_update(entry)
	return
  end

  def self.uses
	ids = {}
	DB.execute( "select * from t_uses" ) do |row|
	  ids[row.first] ||= 0
	  ids[row.first] += 1
	end
	ids.sort
  end

  def self.keys
	ids = []
	DB.execute( "select key from translations" ) { |row| ids << row.first }
	ids.delete_if{ |x| x.nil?}
	ids.sort
  end

  def self.db_insert_or_update(entry)
	res = -1; DB.execute( "select count(*) from translations where key = '#{entry[:key]}'" ) { |row| res = row.first }
	if res == 0
	  DB.execute "insert into translations values ( ?, ? , ? , ? , ?, ?, ?)",
		  entry[:key], entry[:java], entry[:de],
		  entry[:en], entry[:fr], entry[:it], 0
	  @@hasChanges = true
	else
	  @@hasChanges = true
	  DB.execute "update translations set key = ?, java = ? , de = ? , en = ? , fr =  ?,  it = ? where key = '#{entry[:key]}'", 
		  entry[:key], entry[:java], entry[:de],
		  entry[:en], entry[:fr], entry[:it]
	end
	DB.execute( "select * from translations where key = '#{entry[:key]}'" ) { |row| p row } if $VERBOSE
  end
  def self.save_csv
	destFile = DB_NAME.sub(File.extname(DB_NAME),'.csv')
	puts "Saving DB to #{destFile} with changes? #{@@hasChanges} (Takes a few seconds)"
	return unless @@hasChanges || !File.exists?(destFile)
	headers = [:key, :java, :de, :en, :fr, :it]
	CSV.open(destFile, "w") do |csv|
	  csv << headers
	  L10N_Cache.keys.each do |key|
		entry = L10N_Cache.db_get_entry(key)
		csv << entry
	  end
	end
  end
end

class I18nInfo
  attr_accessor :main_dir, :start_dir
  @@all_projects  ||= {}
  @@msg_files_read = []
  Translations = Struct.new(:lang, :values)

  def initialize(directories)
    @@directories = []
    @gen_csv = false
    @root_dir = directories.first
    directories ||= [ ARGV && ARGV[0] ]
    directories  << Dir.pwd if directories.empty?
    directories.each{ |dir| @@directories << File.expand_path(dir) }
    puts "Initialized for #{@@directories.size} directories"
  end

  def get_git_path(filename)
    `git ls-tree --full-name --name-only HEAD #{filename}`.chomp
  end
  def find_translation(key, language, project, filename, line_nr)
    found = @@db_texts.where(:key => key.to_s, :language => language, repository: @repository, project: project, filename:  get_git_path(filename), line_nr: line_nr).all.first
    found ? found[:translation] : nil
  end

  LineSplitter = /\s*=\s*/ # ResourceBundleEditor uses ' = ' as separator, other use '='
  # Options for reading / writing ResourceBundleEditor properties file
  RBE_FILE_OPTIONS_FOR_READ  = 'r:ISO-8859-1'
  #
  
  # replace_dots_by_underscore is necessary when converting old style uses of Messages.java using the getString method
  def get_key_value(line, replace_dots_by_underscore: true)
    begin
      return false if /^#/.match(line)
      return false if line.length <= 1
      line = line.encode("utf-8", replace: nil)
    rescue
      line
    end
    begin
      m = /([^=]*)\s*=\s*(.*)/.match(line.chomp)
      return unless m
      rescue => error
        # Happens with french translat of DataImporter
        line = '%Da' + line[1..-1]
        begin
          m = /([^=]*)\s*=\s*(.*)/.match(line.chomp)
        rescue => error
          binding.pry
        end
    end
    # key has two special thing:
    # * fix some odd occurrences like "BBS_View20\t\t\t "
    # * with Messages.getString(key) it was possible that a key contained a '.', but for as a constant_name we replace it by '_'
    begin
    key = m[1].sub(/\s+$/,'')
    key = key.gsub('.', '_') if replace_dots_by_underscore
    value = m[2].sub(/ \[#{key}\]/,'')
    [key, value]
    rescue => error
      binding.pry
    end
  end

  def analyse_one_message_line(project_name, lang, filename, line_nr, line)
    replace_dots = false if filename.index('/ch/elexis/core/model')
    key, value = get_key_value(line.chomp, replace_dots_by_underscore: replace_dots)
    return unless key
    key = "#{project_name}#{L10N_Cache::TAG_SEPARATOR}#{key}" unless /^messages/i.match(File.basename(filename))
	L10N_Cache.set_translation(key, lang, value)
  end
  
  def parse_plug_properties(project_name, lang, propfile)
    return unless File.exist?(propfile)
    File.open(propfile, RBE_FILE_OPTIONS_FOR_READ).readlines.each do |line|
      key, value = get_key_value(line.chomp, replace_dots_by_underscore: false)
      next unless key
      next if /false/.match(key)
      key = "#{project_name}#{L10N_Cache::TAG_SEPARATOR}#{key}"
      L10N_Cache.set_translation(key, lang, value)
    end
  end
  def parse_plugin_xml(project_name, filename)
    return unless File.exist?(filename)
    keys = {}
    # (?:|label=")|description="|tooltip="|name="|)%([\.\w]+)
    IO.readlines(filename).each do |line|
      if (m = /%([\.\w]+)/i.match(line.chomp))
        key = [project_name, m[1] ].join(L10N_Cache::TAG_SEPARATOR)
        keys[key] = ''
      end
    end;
    mf = filename.sub('plugin.xml', 'META-INF/MANIFEST.MF')
    IO.readlines(mf).each do |line|
      if (m = /:\s+%([\.\w-]+)/i.match(line.chomp))
        key = [project_name, m[1] ].join(L10N_Cache::TAG_SEPARATOR)
        keys[key] = ''
      end
    end;
    parse_plug_properties(project_name, L10N_Cache::JavaLanguage, filename.sub('.xml', '.properties'))
    L10N_Cache::LANGUAGE_KEYS.each do |lang|
      propfile = filename.sub('.xml', "_#{lang}.properties")
      next unless File.exist?(propfile)
      parse_plug_properties(project_name, lang, propfile)
    end
    keys
  end

  def analyse_one_message_file(project_name, filename)
    fullname = File.expand_path(filename)
    if @@msg_files_read.index(fullname)
      puts "Skipping #{fullname}"
	  return
    else
      @@msg_files_read << fullname
    end
    line_nr = 0
    if m = /_(\w\w)\.properties/.match(File.basename(filename))
      language2 = m[1].to_s
    else
      language2 = L10N_Cache::JavaLanguage
    end
    File.open(filename, RBE_FILE_OPTIONS_FOR_READ).readlines.each do |line|
      line_nr += 1
      if analyse_one_message_line(project_name, language2, filename, line_nr, line) && language2.eql?(L10N_Cache::JavaLanguage)
      end
    end if File.exist?(filename)
    puts "#{project_name} added #{filename}" if $VERBOSE
  end

  def get_project_name(project_dir)
    while true
      project_file = File.join(project_dir, ".project")
      break if File.exist?(project_file)
      return nil if  project_dir.eql?(Dir.pwd)
      project_dir = File.dirname(project_dir)
    end
    project_xml = Document.new(File.new(project_file))
    project_xml.elements['projectDescription'].elements['name'].text
  end

  def parse_plugin_and_messages
    @@directories.each do |directory|
      @main_dir = File.expand_path(directory)
      Dir.chdir(@main_dir)
      projects = (Dir.glob("**/.project") + Dir.glob('.project')).uniq.compact
      projects.each do |project|
        Dir.chdir(@main_dir)
        project_dir = File.expand_path(File.dirname(project))
        Dir.chdir(project_dir)
        @repository = calculate_repository_origin(project_dir)
        project_name  = get_project_name(project_dir)
        next if /test.*$|feature/i.match(project_name)
        msg_files = Dir.glob(File.join(project_dir, 'src', '**/messages*.properties'))
        if plugin_xml = File.join(project_dir, 'plugin.xml')
          parse_plugin_xml(project_name, plugin_xml)
        end
        puts "#{directory}: msg_files are #{msg_files}" if $VERBOSE
        if msg_files.size == 0
          puts "Skipping #{Dir.pwd}" if $VERBOSE
        else
          puts "#{Dir.pwd} found #{msg_files.size} messages files"
          msg_files.each{|msg_file| analyse_one_message_file(project_name, msg_file) }
          next
        end
      end
    end
  end

  def calculate_repository_origin(directory = Dir.pwd)
    git_config =  `git config  --local remote.origin.url`.chomp.split('/').last
    unless git_config && git_config.length > 0
      @repository = 'unknown'
    else
      @repository = git_config.sub(/\.git$/, '')
    end
  end

  def start_with_lang_in_parenthesis(line, lang)
  	/^\(#{lang}/.match(line)
  end

  def add_google_translation(source_lang, string2translate, lang)
    unless string2translate
      puts "Skipping #{source_lang} -> #{lang} for #{string2translate} as no source found"
      return
    end
    # translate_text(what, target_language=:it, source_language=:de)
    translated = GoogleTranslation.translate_text(string2translate, lang, source_lang)
	puts "add_google_translation #{source_lang} -> #{lang} for #{string2translate} got #{translated}"
    return unless translated
    CGI.unescapeHTML(translated)
  end

  def add_missing # aka add_csv_to_db_texts
    unless ENV['TRANSLATE_API_KEY']
      puts "Without an enviornment variable TRANSLATE_API_KEY we cannot translate any new string"
      exit(3)
    end
    inserts = {}
    idx = 0
	size = L10N_Cache.keys.size
    puts "Adding missing entries for #{File.expand_path(DB_NAME)} checking #{size} keys"
    L10N_Cache.keys.each do |key|
      puts "Checking #{idx} #{key}" if $VERBOSE
      entry = L10N_Cache.db_get_entry(key)
      idx += 1
      puts "#{Time.now}: Analysing message #{idx} of #{size}" if idx % 500 == 0
	  binding.pry unless key.eql?(key.encode('utf-8'))
      # Ensure that we have a german translation (which is our default language)
	  default = ''
	  orig_lang = :en
      if entry[:java] && entry[:de] && entry[:de].size == 0 && entry[:en] && entry[:en].size == 0
		first_entry = default = entry[:java]
		orig_lang = :de
	  elsif entry[:de] && entry[:de].size > 0
		first_entry = default = entry[:de]
		orig_lang = :de
	  elsif entry[:en] && entry[:en].size > 0
		first_entry = default = entry[:en]
	  end
	  default = entry[:java] if default.size == 0
      L10N_Cache::LANGUAGE_KEYS.each do |lang|
		current_translation = L10N_Cache.get_translation(key, lang)
		next if current_translation && current_translation.size > 0
        if lang.eql?(orig_lang)
            unless current_translation && current_translation.size > 0
                L10N_Cache.set_translation(key, lang, entry[:java])
            end
            next
        end
	    next if lang.eql?(orig_lang)
		# puts "No translation for #{key} given" if default.size == 0
		next if default.size == 0
		translated = add_google_translation(orig_lang, default, lang)
		puts "Adding #{translated} missing translation for #{lang} #{key}" if $VERBOSE
		L10N_Cache.set_translation(key, lang, translated)
		inserts[[key, lang]] =   translated
		puts "#{Time.now}: Added #{inserts.size} new translations" if inserts.size % 100 == 0
      end
    end
    puts "Inserted #{inserts.size} missing entries of #{size}"
  end

  def to_db(dir)
    @@directories = [dir]
    @gen_csv = true
    parse_plugin_and_messages
  end

  def generate_plugin_properties(project_name, filename)
    if project_name.eql?('ch.elexis.core.findings.templates.edit')
      puts "Skipped plugin properties for #{project_name}"
      return
    end
    puts "Generating plugin properties for #{File.expand_path(filename)}" if $VERBOSE
    plugin_key_hash = parse_plugin_xml(project_name, File.join(File.dirname(filename), 'plugin.xml')) || {}
    keys = plugin_key_hash.keys
    L10N_Cache::LANGUAGE_KEYS.each do |lang|
      lang_file = filename.sub('.properties', (lang.eql?(:java) ? '' : '_' + lang.to_s) + '.properties')
      File.open(lang_file, RBE_FILE_OPTIONS_FOR_WRITE) do |file|
        keys.sort.uniq.each do |tag_name|
          next if /_false$/.match(tag_name)
          translations = L10N_Cache.db_get_entry(tag_name)
		  unless translations
            puts "#{project_name}: Missing translation in #{File.basename(lang_file)} for #{tag_name}"
            emit_RBE_compatible_line(file, tag_name, '', true)
            next
          end
          lang_value = translations[lang]
          lang_value = translations[L10N_Cache::JavaLanguage] if !lang_value || lang_value.empty?
		  lang_value ||= translations['en'] if lang.eql?(:java) && translations['en']
          tag2write = tag_name.sub(project_name+L10N_Cache::TAG_SEPARATOR,'')
          next if tag2write.eql?('false')
          next unless keys.find_all{|x| /#{tag_name}$/.match(x)}.size > 0
          if !lang_value || lang_value.empty?
            puts "no #{lang} value found for #{tag2write}"
            next
          end
          emit_RBE_compatible_line(file, tag2write, lang_value, true)
        end
      end
    end
  end
 
  def find_best_name(german)
    cmd = "select key, de from translations where de == '#{german}' and ( key like 'Core_%' or key like 'Sex%' or key like 'Printing_%' or key like 'Export_while' or key like 'Script%' or key like 'UNKNOWN' or key like 'TimeTool%' or key like 'Contact_%');"
    res = DB.execute(cmd)
    if res && res.first
      puts "Best name for #{german} is #{res.first[0]}" if $VERBOSE
      return res.first[0]
    else
      puts "Nothing found for #{german}" if $VERBOSE
      cmd = "select min(key), de from translations where de == '#{german}';"
      res = DB.execute(cmd)
      if res && res.first
        puts "min name for #{german} is #{res.first[0]}" if $VERBOSE
        return res.first[0]
      end
    end
    nil
  end
  
  def eliminate_properties(msg_file, patches)
    prop_files = Dir.glob(msg_file.sub('Messages.java', 'messages**.properties'))
    prop_files.each do |prop_file|
       content = IO.read(prop_file, encoding: "ISO8859-1")
       old_content = content.clone
       patches.each do |old_key, new_key|
         content.gsub!(/^#{old_key}(\s*=\s*[\w\.]+)$/, '')
       end
        File.open(prop_file, 'w+') do |file|
          puts "Patched properties    #{prop_file}"
          file.write content
        end unless content.eql?(old_content)
    end
  end
  
  def eliminate_messages_java(msg_file, patches)
    content = IO.read(msg_file)
    old_content = content.clone
    patches.each do |old_key, new_key|
       next unless content.include?(old_key)
       if /\W#{old_key}\W/.match(content) && !/\W#{new_key}\W/.match(content)
         content.gsub!(/(\W)#{old_key}(\W)/, "\\1#{new_key}\\2")
       end
      content.gsub!(/[^\n]*\s#{old_key}\s*=[^\n]*/, '')
    end
    File.open(msg_file, 'w+') do |file|
      puts "Patched Messages.java #{msg_file}"
      file.write content
    end unless content.eql?(old_content)
  end

  def eliminate_java(java_file, patches)
    return if File.basename(java_file).eql?("Messages.java")
    return if File.dirname(java_file).include?("/src-gen")
    return if File.dirname(java_file).include?("/ch.rgw.utility")
    content = IO.read(java_file)
    old_content = content.clone
    patches.each do |old_key, new_key|
      content.gsub!(/(.*Messages\.)(#{old_key})(\W.*)/, "\\1#{new_key}\\3")
    end
    content.sub!(/import ch.elexis.core.*.Messages;/, 'import ch.elexis.core.l10n.Messages;')
    File.open(java_file, 'w+') do |file|
      puts "Patched Javafile      #{java_file}"
      file.write content
    end unless content.eql?(old_content)
  end

  def eliminate(main_dir)
#      puts find_best_name('unbekannt')
       main_dir = File.expand_path(main_dir)
#   j=0; DB.execute( "select count(de) anzahl, de, min(key), max(key) from translations group by de  having anzahl > 2 order by anzahl desc" ) { |row| j=j+1; break if j == 5; p row }
       puts "Reduce #{main_dir}"
    patches = {}
    DB.execute('select count(*) anzahl, key, de from translations group by de  having anzahl > 2 order by anzahl desc;' ) do |row|
       german = row[2];
       best = find_best_name(german);
       puts "key #{german} => #{best}" if  $VERBOSE
       next if german && german.size == 1
       cmd = "select key from translations where de == '#{german}'"
       DB.execute(cmd) do |entry|
         next if /.*Timeout$/.match(entry.first) # skipe keys like CobasMiraAction_DefaultTimeout
         next if entry.first.eql?(best)
         patches[entry.first] = best
         puts "Must replace #{entry.first} by #{best}" if $VERBOSE
         nrDuplicates = DB.execute("select key from translations where de == '#{german}'").size
         DB.execute("update translations set occurences = #{nrDuplicates} where key == '#{best}'")
         DB.execute("update translations set occurences = -#{nrDuplicates} where de == '#{german}' and key != '#{best}'")
       end
    end
    patches.each do |old_key, new_key|
      puts "Must patch #{old_key} by #{new_key}" if $VERBOSE
    end
     Dir.glob("#{main_dir}/**/Messages.java").each do |msg_java|
       eliminate_properties(msg_java, patches)
       eliminate_messages_java(msg_java, patches)
     end
     Dir.glob("#{main_dir}/**/*.java").each do |java_file|
       eliminate_java(java_file, patches)
     end
  end
  #
  # analyses new messages in main_dir
  # to see whether we have already in l10n occurences with the same
  # text
  def analyze_new(main_dir)
	project_name =  get_project_name(main_dir)
	existing_keys = get_keys_from_messages_java(L10N_MESSAGES, 'l10n')
	update_uses
	used_keys =  L10N_Cache.uses.clone
	unused_keys = existing_keys.select{ |x| ! used_keys.index(x) }
	keys = []
	reported = []
    Dir.glob("#{main_dir}/**/Messages.java").each do |msg_java|
	  puts msg_java
	  some_keys = get_keys_from_messages_java(msg_java, project_name).sort
	   prop_file = msg_java.sub('Messages.java', 'messages.properties')
	   analyse_one_message_file(project_name, prop_file)
#	   puts "found #{some_keys.size} new keys in #{msg_java}"
	   keys += some_keys
	end
	puts "found #{keys.size} new keys in #{project_name}"
	already_present = existing_keys.select{ |x| keys.index(x)}
	new_keys = keys.select{ |x| !already_present.index(x)}
	new_keys.each do |key|
	  text = L10N_Cache.db_get_entry(key)[:java]
	  entries = L10N_Cache.search_text(text, :java)
	  entries.delete_if{|x| x[:key].eql?(key) }
	  next if reported.index(text)
	  reported << text
	  if entries.size > 1
		puts "\nkey #{key}:\n   '#{text}'\n already present as  #{entries.collect{|x| x[:key]}.join("\n   ")}"
	  end
	end
  end

  STANDARDS = Hash.new 
  STANDARDS[/("\\r")/] = "org.apache.commons.lang3.StringUtils.CR"
  #  (?<!pat) is a Negative lookbehind, see https://ruby-doc.org/core-3.1.2/Regexp.html
  STANDARDS[/(?<!\\)""/] = "org.apache.commons.lang3.StringUtils.EMPTY"
  STANDARDS[/("\\n")/] = "org.apache.commons.lang3.StringUtils.LF"
  STANDARDS[/(" ")/] = "org.apache.commons.lang3.StringUtils.SPACE"
	    
  def standardize_one_item(content, key, value)
	m = key.match(content)
	return false unless m
	toImport = value.split('.')[0..-2].join('.')
	if m 
	   short = value.split('.')[-2..-1].join('.')
	   content.gsub!(key, short)
	   unless content.include?(toImport)
        if /^(\s*import.*)$/.match(content)
		  content.sub!(/^(\s*import.*)$/, "\nimport #{toImport};\\1")
		else # no import statement found
         content.sub!(/^.*(package.*;)/, "\\1\nimport #{toImport};")
		end
	   end
	end
	true
  end

  def standardize(main_dir)
	@has_changes = false
	@has_non_nls = false
    Dir.glob("#{main_dir}/**/*.java").each do |javafile|
	  content = File.read(javafile)
      old_content = content.clone
	  STANDARDS.each do |key, value |
		@has_changes = true if standardize_one_item(content, key, value)
	  end
	  if @has_changes
		File.open(javafile, 'w') do |file|
            puts "Patched Javafile      #{java_file}"
            file.write(content)
        end unless content.eql?(old_content)
        end
	  lines = File.readlines(javafile)
	  lines.each_with_index do |line, idx| 
		if m = /StringUtils\..*(\s*\/\/\s*\$NON-NLS.*)/.match(line)
		  # puts "#{javafile} with #{line}"
		  lines[idx] = line.sub(m[1], '').rstrip + "\n"
		  # puts "#{javafile} now: #{line}"
	      @has_non_nls = true
		end
	  end
	  if @has_non_nls
		File.open(javafile, 'w') { |file| file.write(lines.join("")) }
	  end
	  puts "Patched #{javafile}" if @has_changes || @has_non_nls
	end
  end

  def to_utf(string)
    begin
      /String\s+(\w+)\s*;/.match(string)
    rescue
      string = string.encode('UTF-8', 'ISO-8859-1')
    end
    string
  end
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

  def emit_l10n
    path = "**/ch.elexis.core.l10n/**/Messages.java"
    msg_java = Dir.glob(path).first
    raise "Unable to find l10n Messages.java (Searched via #{path}" unless msg_java 
	keys = L10N_Cache.keys.find_all{ |key| !/#{L10N_Cache::TAG_SEPARATOR}/.match(key)}.sort
    keys.delete_if{|x| x.index('.')}
	emit_l10_messages_java(msg_java, keys)
	write_translation_to_properties(msg_java, keys)
  end

  def patch_messages_java(main_dir)
    Dir.glob("#{main_dir}/**/Messages.java").each do |msg_java|
      project_name =  get_project_name(msg_java)
      if /core.10n/i.match(project_name)
        puts "to_messages_properties skips project #{project_name} because its name matches 10n"
		next
      end
	  puts "Handling #{project_name}"
      keys = get_keys_from_messages_java(msg_java, project_name).sort
      next if keys.size == 0
	  patch_a_messages_java(msg_java, keys)
    end
  end

  def write_translation_to_properties(msg_java, keys)
	KEYS.each do |lang|
	  lang_file = msg_java.sub('Messages', 'messages').sub('.java', (lang.eql?(:java) ? '' : '_' + lang.to_s) + '.properties')
	  puts "to_messages_properties: Generating #{lang} #{lang_file} using #{msg_java}" if $VERBOSE
	  File.open(lang_file, RBE_FILE_OPTIONS_FOR_WRITE) do |file|
		keys.each do |tag_name|
		  next if /_false$/.match(tag_name)
		  next if tag_name.eql?('BUNDLE_NAME')
		  translations =   L10N_Cache.db_get_entry(tag_name)
		  unless translations
			puts "Missing translation in #{File.basename(lang_file)} for #{tag_name}"
			emit_RBE_compatible_line(file, tag_name, '')
			next
		  end
		  lang_value = translations[lang]
		  lang_value = translations[L10N_Cache::JavaLanguage] if !lang_value || lang_value.empty?
		  lang_value ||= translations['en'] if lang.eql?(:java) && translations['en']
		  tag2write = tag_name.sub(/.*:/,'')
		  next if tag2write.eql?('false')
		  if !lang_value || lang_value.empty?
			puts "no #{lang} value found for #{tag2write}"
			next
		  end
		  emit_RBE_compatible_line(file, tag2write, lang_value)
		end
	  end
	end
  end
  
  def get_keys_from_messages_java(msg_java, project_name)
	lines = File.readlines(msg_java).collect{|line| to_utf(line) }
    keys = lines.collect{|line| m = L10N_Cache::KEY_REGEX_IN_MESSAGES.match(line); m[1] if m }.compact
    puts "#{project_name}: where #{msg_java} has #{keys.size} keys" if $VERBOSE
    keys
  end
  
 # TODO: Generate properties files for all languages by default, but do correct stuff in l10n.{lang}
  def to_plugin_properties(main_dir)
#    Dir.chdir(main_dir)
	main_dir = File.expand_path(main_dir)
    all_keys = L10N_Cache.keys.collect{|x| x }.uniq
    l10n_key =  all_keys.find{|x| /l10n$/.match(x)}
    # raise("Could not find the main l10n project among #{all_keys}") unless l10n_key
    Dir.glob("#{main_dir}/**/.project").each do |project|
      Dir.chdir(File.dirname(project))
      project_name  = get_project_name(File.dirname(project))
      puts "project_name is #{project_name}" if $VERBOSE
      files = Dir.glob(File.join(Dir.pwd, 'plugin.properties')) 
      files.each do |filename|
        puts "Fixing plugin for #{filename}" if $VERBOSE
        next if filename.split('.').index('target')
        generate_plugin_properties(project_name, filename) 
      end
      files = Dir.glob(File.join(Dir.pwd, '**/messages*.properties'))
      if Dir.pwd.index(/l10n\.[a-zA-Z]{2}$/) && files.size == 0
        raise "You must place a correct messages.properties into #{Dir.pwd}"
      end
    end
  end
  def patch_a_messages_java(msg_java, keys)
    content = IO.read(msg_java)
    old_content = content.clone
	content.sub!(/^\s*(private|static) static final String BUNDLE_NAME.*/, '')
	content.sub!(/^\s*static.*BUNDLE_NAME[^}]+}/m, '')
	content.sub!(/^\s*private Messages[^}]+}/m, '')
	content.sub!(/\s*extends\s+NLS\s+/, '')
    content.gsub!(/(\s*=\s*[\w\.]+)/, '')
#    content.gsub!(/public static String/, 'public String')
	content.gsub!(/^\s*public\s+static\s+String\s+(\w+);/, '    public static String \1 = ch.elexis.core.l10n.Messages.\1;');
#	content.gsub!(/String\s+(\w+)\w*;/, 'String \1 = ch.elexis.core.l10n.Messages.\1;');
    File.open(msg_java, 'w+') do |file|
      puts "Patched Javafile      #{java_file}"
      file.write content
    end unless content.eql?(old_content)
  end
end
	                  
parser = Optimist::Parser.new do
  version "#{File.basename(__FILE__, '.rb')} (c) 2017-2022 by Niklaus Giger <niklaus.giger@member.fsf.org>"
  banner <<-EOS
#{version}
License: Eclipse Public License 1.0 (EPL)
Useage: #{File.basename(__FILE__)} [-options] [directory1 directory]
  help manipulating files needed for translations
  using Cachefile        #{GoogleTranslation::CacheFileCSV} (UTF-8)
EOS
  opt :to_db   ,         "Create #{File.basename(DB_NAME)} for all languages with entries for all [manifests|plugin]*.properties ", :default => false, :short => '-c'
  opt :add_missing,       "Add missing translations into #{File.basename(DB_NAME)} via Googe Translator
	                                 using #{GoogleTranslation::CacheFileCSV}", :default => nil, :short => '-a'
  opt :to_plugin_properties,     "Create plugin*.properties   for all languages from #{File.basename(DB_NAME)}", :default => false, :short => '-p'
  opt :patch_messages,     "Patch Messages.java to import all variable from ch.elexis.core.l10n.Messages", :default => false, :short => '-m'
  opt :emit_l10n, "Create Messages.java + properties for l10n", :default => false, :short => '-e'
  opt :standardize, "Use some string constant from apache.commons in all java files in given subdir", :default => false, :short => '-s'
  opt :analyze_new, "Get new keys and find already existing ones", :default => false, :short => "-n"
  opt :eliminate, "Eliminate duplicates by best name", :default => false
end

Options = Optimist::with_standard_exception_handling parser do
  raise Optimist::HelpNeeded if ARGV.empty? # show help screen
  parser.parse ARGV
end
# GoogleTranslation.translate_text('Gutschrift')
# GoogleTranslation.translate_text('elektronische Krankengeschichte')

i18n = I18nInfo.new(ARGV)
i18n.start_dir = Dir.pwd
if ARGV.size > 0
  i18n.main_dir = File.expand_path(ARGV.first)
  raise "We expected #{ARGV.first} to be the name of an existing directory" unless File.directory?(i18n.main_dir)
else
  i18n.main_dir = Dir.pwd
end unless defined?(RSpec)
saved_pwd = Dir.pwd

ARGV.each do |dir|
	Dir.chdir saved_pwd
	puts "Handling #{dir}"
    if Options[:eliminate]
       i18n.eliminate(dir)
       puts "Done"
       exit
    end
	i18n.to_db(dir) if Options[:to_db]
	i18n.standardize(dir) if Options[:standardize]
	i18n.to_messages_properties(dir)  if Options[:to_messages_properties]
	i18n.to_plugin_properties(dir) if Options[:to_plugin_properties]
	i18n.patch_messages_java(dir) if Options[:patch_messages]
	i18n.emit_l10n if Options[:emit_l10n]
	i18n.analyze_new(dir) if Options[:analyze_new]
end
	   # update_uses
i18n.add_missing if Options[:add_missing]
L10N_Cache.save_csv
GoogleTranslation.save_cache
