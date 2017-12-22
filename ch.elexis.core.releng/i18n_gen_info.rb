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
# result = translate.list_translations('Hello world!', 'es', source: 'en')
# puts result.translations.first.translated_text
# to connect to the database
# sudo apt-get install sqlite3 unixodbc unixodbc-bin libreoffice-base-drivers
# /etc/odbcinst.ini aufgefüllt gemäss https://wiki.openoffice.org/wiki/Documentation/How_Tos/Using_SQLite_With_OpenOffice.org#SQLite_ODBC_Driver
#
require 'google/apis/translate_v2'
require "rexml/document"
include REXML  # so that we don't have to prefix everything with REXML::...
require 'ostruct'
require 'pp'
require 'uri'
require 'pry-byebug'
require 'csv'
require 'net/http'
require 'json'
require 'sequel'
require 'sqlite3'
require 'trollop'
require 'logger'
$stdout.sync = true
Sqlite3File = File.join(Dir.home, 'elexis-translation.db')

class GoogleTranslation
  @@updated_cache = false
  BaseLanguageURL='https://translate.googleapis.com/translate_a/single?client=gtx&sl='
  Translate = Google::Apis::TranslateV2::TranslateService.new
  Translate.key = ENV['TRANSLATE_API_KEY']
    # url = "#{BaseLanguageURL}#{source_language}&format=json&tl=#{target_language}&dt=t&q=#{URI.encode(what)}"
    # uri = URI(url)
    # response = Net::HTTP.get(uri)
    # JSON.parse(response)

  def self.translate_text(what, target_language='it', source_language='de')
    key = [what, target_language, source_language]
    unless @@translationCache[key]
      begin
        value = Translate.list_translations(what, target_language, source: source_language)
        @@translationCache[key] = value.translations.collect{|x| x.translated_text}
        puts "Added #{key} #{@@translationCache[key]}"
        @@updated_cache = true
      rescue => error
        puts error
        puts "translate_text failed. Is environment variable TRANSLATE_API_KEY not specified?"
        exit
      end
    end
    @@translationCache[key]
  end

  @@use_yaml = false
  CacheFileYaml = File.join(Dir.home, 'google_translation_cache.yaml')
  CacheFileCSV = File.join(Dir.home, 'google_translation_cache.csv')

  def self.load_cache
    @@translationCache = {}
    if @@use_yaml || (!File.exist?(CacheFileCSV) && File.exist?(CacheFileYaml))
      @@translationCache = File.exist?(CacheFileYaml) ? YAML.load_file(CacheFileYaml) : {}
    elsif File.exist?(CacheFileCSV)
      CSV.foreach(CacheFileCSV, :force_quotes => true) do |cells|
        next if cells[0].eql?('src')
        key = [cells[0], cells[1], cells[2]]
        value = cells[3] ? cells[3].chomp : ''
        @@translationCache[key] = [value]
      end
    end
  end

  def self.save_cache
    return unless @@updated_cache
    puts "Saving #{@@translationCache.size} entries to #{CacheFileYaml}"
    File.open(CacheFileYaml,'w+') do |h|
      h.write @@translationCache.to_yaml
    end if false
    CSV.open(CacheFileCSV, "wb", :force_quotes => true) do |csv|
      csv << ['src', 'dst', 'what', 'translated']
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
  at_exit do GoogleTranslation.save_cache end
end

class I18nInfo
  attr_accessor :main_dir, :start_dir
  @@all_msgs      ||= {}
  @@all_projects  ||= {}
  @@msg_files_read = []
  @@nr_duplicates  = 0
  @@nr_skipped     = 0
  @@nr_inserted    = 0
  LanguageViews = { 'de' => 'german',
            'fr' => 'french',
            'it' => 'italian',
            'en' => 'english',
            }

  MainLanguage = 'Java'
  Trema_Default_Language = 'de'
  Translations = Struct.new(:lang, :values)

  CSV_KEYS = LanguageViews.keys + [MainLanguage]
  CSV_NAME = 'translations.csv'
  CSV_HEADER_START = ['project', 'translation_key']
  REGEX_TRAILING_LANG = /\.plugin$|\.[a-z]{2}$/
  def self.all_msgs
    @@all_msgs
  end
  def initialize(directories)
    @@directories = []
    @gen_csv = false
    directories ||= [ ARGV && ARGV[0] ]
    directories.each{ |dir| @@directories << File.expand_path(dir) }
    puts "Initialized for #{@@directories.size} directories"
  end

  # DB = Sequel.sqlite # memory database, requires sqlite3
  # http://sequel.jeremyevans.net/rdoc/classes/Sequel/Database.html
  # DB.create_view(:checked_items, DB[:items].where(:foo), :check=>true)
  DB = Sequel.sqlite(Sqlite3File)
  # DB.loggers << Logger.new($stdout)
  DB.create_table? :db_texts do
    primary_key :id
    String :key
    String :language, :default => 'de'
    String :project # eg. ch.elexis.core.ui or ch.elexis.core.ui_plugin
    String :repository, :default => 'elexis-3-core' # eg elexis-3-core
    String :translation
    String :status, :default => 'initial'
    String :filename
    String :line_nr
    String :google_translation, :default => nil
    index [ :key, :language, :project, :repository, :translation], :unique => true
  end
  DB.create_table? :duplicates do
    primary_key :id
    String :key
    String :language
    index [ :key, :language], :unique => true
  end
  some_util_sql_commands = %(
select key, count(translation) german_duplicates from german group by key having german_duplicates > 1;

select key, language, translation, filename, line_nr from db_texts where key in \
    (select key from german group by key having count(translation)  > 1) \
    order by key, language, translation;

select german.key, german.translation, db_texts.project, db_texts.filename from german, italian, db_texts
  where german.key = italian.key and and german.translation = italian.translation and
    german.key = db_texts.key and and german.translation = db_texts.translation and
  ;

# same translation for italian as german
select german.key, italian.translation as italian, db_texts.project, db_texts.filename from german, italian, db_texts
  where german.key = italian.key and german.translation = italian.translation and
    german.key = db_texts.key and german.translation = db_texts.translation ;

# same translation for french as german
select db_texts.project, german.key, french.translation as french, db_texts.filename from german, french, db_texts
  where german.key = french.key and german.translation = french.translation and
    german.key = db_texts.key and german.translation = db_texts.translation
  order by db_texts.project, french.key;

# same translation for english as german
select db_texts.project, german.key, english.translation as english, db_texts.filename from german, english, db_texts
  where german.key = english.key and german.translation = english.translation and
    german.key = db_texts.key and german.translation = db_texts.translation
  order by db_texts.project, english.key;

)
  LanguageViews.each do |key, name|
    DB.create_or_replace_view(name.to_sym, "select distinct key, translation, google_translation from db_texts where language = '#{key}'")
  end
  DB.create_or_replace_view(:all_languages, %(select distinct
                              german.translation as 'Deutsch',
                              french.translation as 'Français',
                              english.translation as 'Englisch'
                            from german, french, english
                           where french.key = german.key and english.key = german.key))
  DB.create_or_replace_view(:same_de_fr_en, %(select german.key,
                            german.translation as 'Deutsch',
                            french.translation as 'Français',
                           english.translation as 'Englisch'
                           from german, french, english
                           where french.key = german.key and english.key = german.key and
                            german.translation = french.translation and german.translation = english.translation))

  # DB = Sequel.sqlite # memory database, requires sqlite3
  @@db_texts = DB[:db_texts] # Create a dataset
  @@duplicates = DB[:duplicates] # Create a dataset

  def self.all_msgs
    @@all_msgs
  end
  all_msgs_info = %(
    key => [languages] [filename, line, translated]
  )

  def insert_translation(key, language3, project, filename, line_nr, translation)
    begin
      @@db_texts.insert(key: key,
                      language: language3,
                      repository: @repository,
                      project: project,
                      filename:  get_git_path(filename),
                      line_nr: line_nr,
                      translation: translation)
    rescue => error
      #  UNIQUE constraint failed: db_texts.key, db_texts.language, db_texts.project, db_texts.repository, db_texts.translation
      # puts "error #{error}" # Happens if same key exists twice in the same file
      # condition = {:key => key, :language => language3, repository: @repository, project: project,                        filename:  get_git_path(filename)}
      condition = {:key => key, :language => language3, repository: @repository, project: project, translation: translation}
      duplicates = @@db_texts.where(condition)
      puts "duplicate? #{key} #{language3} #{@repository} #{project} #{} #{get_git_path(filename)}. Update line_nr #{duplicates.first[:line_nr]}->  #{line_nr}" if duplicates.first
      begin
        @@duplicates.insert(key: key, language: language3)
      end
    end
  end

  def get_git_path(filename)
    `git ls-tree --full-name --name-only HEAD #{filename}`.chomp
  end
  def find_translation  (key, language, project, filename, line_nr)
    found = @@db_texts.where(:key => key.to_s, :language => language, repository: @repository, project: project, filename:  get_git_path(filename), line_nr: line_nr).all.first
    found ? found[:translation] : nil
  end
  EscapeBackslash = /\\([\\]+)/
  LineSplitter = /\s*=\s*/ # ResourceBundleEditor uses ' = ' as separator, other use '='
  #
  # Converts escapces like \u00 to UTF-8 and removes all duplicated backslash.
  # Verifiy it using the following SQL scripts
  # select * from db_texts where translation like '%\u00%';
  # select * from db_texts where translation like '%\\%';
  def convert_to_real_utf(string)
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
  
  def get_key_value(line)
    begin
      return false if /^#/.match(line)
      return false if line.length <= 1
      line = URI.decode line.encode("utf-8", replace: nil)
    rescue => e
      line
    end
    m = /([^=]*)\s*=\s*(.*)/.match(line.chomp)
    # key has two special thing:
    # * fix some odd occurrences like "BBS_View20\t\t\t "
    # * with Messages.getString(key) it was possible that a key contained a '.', but for as a constant_name we replace it by '_'
    key = m[1].sub(/\s+$/,'').gsub('.', '_')
    value = m[2].sub(/ \[#{key}\]/,'')
    [key, value]
  end

  def analyse_one_message_line(project, language1, filename, line_nr, line)
    key, value = get_key_value(line)
    translation = convert_to_real_utf(value)
    project_name = project[:name]
    project_name += '.plugin' if /plugin.*properties/.match(File.basename(filename))
    this_msg =  OpenStruct.new ( { :language => language1.clone,
                                   :project => project_name,
                          :filename =>  get_git_path(filename),
                          :line_nr => line_nr,
                          :translation => translation })
    if (existing = find_translation(key, language1, project_name, filename, line_nr))
      @@nr_skipped += 1
      puts "Already skipped #{@@nr_skipped}. In project #{project_name}" if @@nr_skipped % 100 == 0 && $VERBOSE
      $stdout.write '.' if @@nr_skipped % 100 == 0
    else
      insert_translation(key, language1, project_name, filename, line_nr, translation)
      @@nr_inserted += 1
      puts "Inserted #{@@nr_inserted}. In project #{project_name}" if @@nr_inserted % 100 == 0 && $VERBOSE
      $stdout.write '.' if @@nr_inserted % 100 == 0
    end unless @gen_csv
    @@all_msgs[key] ||= {}
    @@all_msgs[key] [language1] ||= {}
    # if key.eql?('Ablauf_cachelifetime') && language1.eql?('de')
    if @@all_msgs[key][language1] && @@all_msgs[key][language1][:translation]
      puts "#{project_name} #{project[:nr_duplicates]}: duplicate key #{language1} #{key} in #{filename} #{line_nr}" if $VERBOSE
      project[:nr_duplicates] += 1
      @@nr_duplicates += 1
      @@all_msgs[key][language1][:duplicates] ||= []
      @@all_msgs[key][language1][:duplicates] << this_msg
    else
      @@all_msgs[key][language1] = this_msg
    end
    return true
  rescue => error
  end

  def update_project_info(project)
    project[:nr_java_files] = Dir.glob(File.join(project[:directory], '**/*.java')).size
  end

  def analyse_one_message_file(project_name, filename)
    fullname = File.expand_path(filename)
    if @@msg_files_read.index(fullname)
      puts "Skipping #{fullname}"
    else
      @@msg_files_read << fullname
    end
    project = @@all_projects[project_name]
    info = OpenStruct.new
    info[:filename] = filename
    # info[:nr_msgs] = IO.readlines(filename).size
    line_nr = 0
    if m = /_(\w\w)\.properties/.match(File.basename(filename))
      language2 = m[1].to_s
    else
      language2 = MainLanguage
    end
    File.open(filename, 'r:ISO-8859-1').readlines.each do |line|
      line_nr += 1
      if analyse_one_message_line(project, language2, filename, line_nr, line) && language2.eql?(MainLanguage)
        project[:nr_msgs]  += 1
      end
    end
    update_project_info(project)
    puts "#{project_name} added #{filename} info #{info}" if $VERBOSE
  end

  def project_info(projectname)
    project = @@all_projects[projectname]
    info = []
    info << "Project #{projectname} in #{project[:directory]}"
    info << "  has #{project[:nr_java_files]} java files"
    info << "  has #{project[:msg_files].size} message files with #{project[:nr_msgs]} messages"
    info
  end

  def overview
    nr_java = 0
    @@all_projects.values.collect{ |x| x[:nr_java_files]}.compact.each{|x| nr_java += x}
    nr_msg_files = @@all_projects.values.collect{ |x| x[:msg_files]}.flatten.compact.size
    info = []
    info << "Overview of Elexis internationalization in #{@main_dir}"
    info << ''
    info << "  has #{@@all_projects.keys.size} projects with #{nr_java} Java and #{nr_msg_files} message files"
    info << "  has languages: #{@@all_msgs.values.collect{|v,k| v.keys}.flatten.uniq.sort.join(', ')}"
    info << "  has #{@@all_msgs.keys.size} messages and #{@@nr_duplicates} duplicates"
    info << ''
    info
  end

  def info_per_project
    info << 'Info per project'
    info << ''
    @@all_projects.each{|name, project| info += project_info(name)}
  end

  def gen_csv(filename: nil)
    unless filename
      puts"Skipping gen_csv as no filename given"
      return
    end
    puts "gen_csv #{filename}"
    CSV.open(filename, "wb") do |csv|
      csv << ['project_name', 'directory', 'nr_java_files', 'msg_files', 'nr_msgs', 'nr_duplicates']
      @@all_projects.each do |name, info|
        csv << [name, info[:directory],
                info[:nr_java_files] ? info[:nr_java_files] : 0,
                info[:msg_files].size,
                info[:nr_msgs],
                info[:nr_duplicates]]
      end
    end
    filename
  end

  def show_dupl(origin, a_duplicate)
    origin = "#{origin[:language]}"

# => #<OpenStruct language="fr", project="ch.elexis.core.ui", filename="/opt/elexis-3.1/elexis-3-core/ch.elexis.core.ui/src/ch/elexis/core/ui/wizards/messages_fr.properties", line_nr=1, translation="">
  end
  def gen_duplicates
    filename = File.expand_path(File.join(@main_dir, File.basename(@main_dir) + '_duplicates.csv'))
    CSV.open(filename, "wb") do |csv|
      csv << ['file', 'line_nr', 'language', 'translation', 'translation2', 'file2', 'line_nr2']
      @@all_msgs.sort.each do |key, messages|
        next if messages.values.collect{|v| v[:duplicates]}.compact.size == 0
        messages.each do |language, info|
          next unless info && info[:duplicates] && info[:duplicates].size > 0
          info[:duplicates].each do |a_duplicate|
            csv << [info[:filename],
                    info[:line_nr],
                    language,
                    info[:translation],
                    a_duplicate[:translation],
                    a_duplicate[:filename],
                    a_duplicate[:line_nr],
                  ]
          end
        end
      end
    end
    filename
  end

  def parse(gen_csv: false)
    start_dir = Dir.pwd
    @@directories.each do |directory|
      @main_dir = File.expand_path(directory)
      Dir.chdir(@main_dir)
      projects = (Dir.glob("**/.project") + Dir.glob('.project')).uniq.compact
      projects.each do |project|
        Dir.chdir(@main_dir)
        project_dir = File.expand_path(File.dirname(project))
        Dir.chdir(project_dir)
        @repository = calculate_repository_origin(project_dir)
        project_xml = Document.new(File.new(File.join(project_dir, ".project")))
        project_name  = project_xml.elements['projectDescription'].elements['name'].text
        next if /test.*$|feature/i.match(project_name)
        @@all_projects[project_name] = my_project = OpenStruct.new
        my_project
        my_project[:msg_files]      = {}
        my_project[:nr_msgs]        = 0
        my_project[:nr_duplicates]  = 0
        my_project_name             = project_name
        my_project[:directory]      = project_dir
        my_project[:name]           = my_project_name
        msg_files = Dir.glob(File.join(project_dir, 'src', '**/messages*.properties')) + Dir.glob(File.join(project_dir, 'plugin*.properties'))
        my_project[:msg_files]        = msg_files.compact
        puts "msg_files are #{msg_files}" if $VERBOSE
        if gen_csv
          if msg_files.size == 0
            puts "Skipping #{Dir.pwd}" if $VERBOSE
          else
            puts "#{Dir.pwd} found #{msg_files.size} messages files"
            msg_files.each{|msg_file| analyse_one_message_file(project_name, msg_file) }
            next
          end
        else
          DB.transaction do
            msg_files.each{|msg_file| analyse_one_message_file(project_name, msg_file) }
          end
        end
      end
    end
    gen_languages_csv(File.join(start_dir, CSV_NAME)) if gen_csv
  end

  def calculate_repository_origin(directory = Dir.pwd)
    git_config =  `git config  --local remote.origin.url`.chomp.split('/').last
    unless git_config && git_config.length > 0
      @repository = 'unknown'
    else
      @repository = git_config.sub(/\.git$/, '')
    end
  end

  MESSAGES_GET_STRING_REGEXP = /Messages.getString\("([^"]*)"\)/
  def fix_messages_get_string(a_java_file)
    inhalt = IO.read(a_java_file)
    while m = MESSAGES_GET_STRING_REGEXP.match(inhalt)
      inhalt.sub!(m[0], 'Messages.'+m[1].sub('.','_'))
    end
    File.open(a_java_file, 'w') {|f| f.write inhalt}
    puts "Fixed Messages.getString #{a_java_file}"
  end

  TRANSLATE_REGEXP = /(%\w*)[.]/
  def fix_plugin_messages_id(plugin_file)
    return unless File.exist?(plugin_file)
    inhalt = IO.read(plugin_file)
    needs_update = false
    while m = TRANSLATE_REGEXP.match(inhalt)
      needs_update = true
      inhalt.sub!(m[0], m[1] +'_')
    end
    return unless needs_update
    File.open(plugin_file, 'w') {|f| f.write inhalt}
    puts "Fixed #{plugin_file}"
  end

  def gen_messages_java(filename, project_name)
    # select * from db_texts where key like '%.%';
    condition = {repository: @repository, project: project_name}
    translations = @@db_texts.where(condition)
    return if translations.all.size == 0 # nothing to do
    all_java_files = Dir.glob('**/*.java')
    if res = system("grep Messages.getString #{all_java_files.join(' ')} 2>&1>/dev/null")
      all_java_files.each do |a_java_file|
        if system("grep Messages.getString #{a_java_file} 2>&1>/dev/null")
          fix_messages_get_string(a_java_file)
        end
      end
    end
    java_file = File.expand_path(filename)
    FileUtils.makedirs(File.dirname(java_file))
    uses_rb = system("grep java.util.ResourceBundle #{java_file} 2>/dev/null")
    if !File.exist?(java_file) || uses_rb
      File.open(java_file, 'w') do |java|
        java.puts %(/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 **********************************************************************/
package #{project_name};

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
  private static final String BUNDLE_NAME = "#{project_name}.messages";
)
      translations.all.collect{|x| x[:key]}.uniq.sort.each do |key|
        unless key.index('.')
          java.puts "    public static String #{key};"
        end
      end
         java.puts %(
    static { // load message values from bundle file
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }
})
      end
      puts "Generated #{java_file}"
    end
  end
  def gen_trema_for_project(filename, project_name = nil)
    # trema_xml = Document.new(File.new(File.join(project_dir, "texts.trm")))
    texts_file = File.expand_path(filename)
    texts = Document.new Header, { :compress_whitespace => %w{value} }
    if project_name
      condition = {repository: @repository, project: project_name}
      translations = @@db_texts.where(condition)
      if /\.plugin/.match(project_name)
        fix_plugin_messages_id(File.join(File.dirname(filename), 'plugin.xml'))
      else
        java_name = File.join(File.dirname(filename), 'src', project_name.gsub('.', '/'), 'Messages.java')
        gen_messages_java(java_name, project_name)
      end
    else
      translations = @@db_texts
    end
    lang_trans = {}
    nr_msgs = 0
    translations.order(:key).each do |db_text|
      lang_trans[db_text[:key]] ||= {}
      if db_text[:language].eql?(MainLanguage)
        # set default values
        LanguageViews.keys.each do |lang|
          lang_trans[db_text[:key]] [lang] ||= db_text[:translation]
        end
      else
        lang_trans[db_text[:key]] [db_text[:language]] = db_text[:translation]
      end
    end
    emittedkeys = []
    lang_trans.each do |key, messages|
      text = Element.new "text"
      text.attributes['key']= key.strip
      next if emittedkeys.index(key.strip)
      emittedkeys << key.strip
      text.add Element.new "context"
      messages.each do |language, translated_msg|
        translation = Element.new 'value'
        translation.attributes['lang'] = language.eql?(MainLanguage) ? Trema_Default_Language : language
        translation.attributes['status'] = "initial"
        translation.text = translated_msg.force_encoding(TremaEncoding)
        text.add translation
        nr_msgs += 1
      end
      texts.root.elements << text
    end
    if nr_msgs > 0
      begin
        File.open(texts_file, "w:#{TremaEncoding}") { |out| texts.write( out, 0, true) } # XML pretty print, transitive to avoid adding new line in text
        puts "Generated #{texts_file} with #{nr_msgs} nr_msgs"
      rescue => error
        puts "Failed to write #{texts_file} error: #{error}"
        binding.pry
      end
    end
  end
  def gen_trema
    gen_trema_for_project(File.join(Dir.home, 'trema.trm'))
    @@directories.each do |directory|
      Dir.chdir(directory)
      projects = (Dir.glob("**/.project") + Dir.glob('.project')).uniq.compact
      puts "Generating trema files for #{projects.size} projects"
      projects.each do |project|
        @repository = calculate_repository_origin
        nr_msgs = 0
        project_dir = File.expand_path(File.dirname(project))
        # next unless project_dir.eql?('ch.elexis.core.ui')
        project_xml = Document.new(File.new(File.join(project_dir, ".project")))
        project_name  = project_xml.elements['projectDescription'].elements['name'].text
        next if /test|feature/.match(project_name)
        plugin_name  = project_name + '.plugin'
        # trema_xml = Document.new(File.new(File.join(project_dir, "texts.trm")))
        gen_trema_for_project(File.expand_path(File.join(project_dir, 'texts.trm')), project_name)
        gen_trema_for_project(File.expand_path(File.join(project_dir, 'plugin.trm')), plugin_name)
      end
    end
  end
  def add_missing(language)
    main_language = 'de'
    DB.transaction do
      to_translate = DB[:db_texts].where(:language => main_language)
      puts "We want to translate #{to_translate.all.size} items for #{main_language}"
      to_translate.each do |elem|
        german =  elem[:translation]
        gt = GoogleTranslation.translate_text(german, language, main_language)
        result = []; gt.each{|x| result << CGI.unescapeHTML(x)}
        gt_translations = result.join(',')
        new_filename = elem[:filename].sub("_#{main_language}","_#{language}")
        x = find_translation(elem[:key], language,  elem[:project], new_filename, elem[:line_nr])
        alread_present = {:key => elem[:key], :language => language,  project: elem[:project], repository: elem[:repository]}
        puts "Translating #{german} found #{@@db_texts.where(alread_present).all.size} was #{german}" #  if $VERBOSE
        if @@db_texts.where(alread_present).all.size == 0
          @@db_texts.insert(key: elem[:key],
                          language: language,
                          repository: elem[:repository],
                          project: elem[:project],
                          filename:  new_filename,
                          line_nr: elem[:line_nr],
                          translation: result,
                          google_translation: gt_translations)
        elsif  @@db_texts.where(alread_present).all.size == 1
          @@db_texts.where(alread_present).update(:google_translation => gt_translations)
        else
          puts "Translating #{german} found #{@@db_texts.where(alread_present).all.size}"
          # select * from db_texts where language = 'it';
        end
      end
    end
  end

  def gen_languages_csv(filename)
    CSV.open(filename, "wb") do |csv|
      csv <<  (CSV_HEADER_START + CSV_KEYS)
      @@all_msgs.each do |key, info|
        # shorten l10n.de -> l0n.de 
        project_name = info.values.first.project.sub(REGEX_TRAILING_LANG, '')
        texts = [project_name, key ]
        CSV_KEYS.each do |lang|
          entry = info[lang] ? convert_to_real_utf(info[lang].translation) : ''
          entry = entry.gsub("\n", '\\n')  # Add multiline to csv
          texts << entry
        end
        csv << texts
      end
    end
    puts "gen_languages_csv created #{filename}"
    filename
  end

  def to_csv
    @@directories = [main_dir]
    @gen_csv = true
    parse(gen_csv: true)
  end

  def to_properties
    Dir.chdir(main_dir)
    @@all_msgs = {}
    index = 0
    languages = []
    CSV.foreach(File.join(start_dir, CSV_NAME), :force_quotes => true) do |cells|
      index += 1 
      if index == 1
        raise("invalid header #{cells}") unless cells[0..1] == CSV_HEADER_START
        languages = cells[2..-1]
        next
      end
      key = cells[0..1]
      @@all_msgs[key] ||= {}
      languages.each_with_index do |lang, index|
        @@all_msgs[key][lang] = "#{cells[index + CSV_HEADER_START.size]}"
      end
    end
    Dir.glob("#{main_dir}/**/.project").each do |project|
      Dir.chdir(File.dirname(project))
      project_name = Dir.pwd.split('/').last.sub(REGEX_TRAILING_LANG, '')
      puts "project_name is #{project_name}" if $VERBOSE
      files = Dir.glob('**/plugin*.properties') +  Dir.glob('**/messages*.properties')
      files.each do |filename|
        keys = []
        puts "Generating #{File.expand_path(filename)}"
        m =  /_(..)\.properties/.match(filename)
        lang = m ?  m[1] : 'en'
        if /messages/.match(File.basename(filename))
          full =File.join(Dir.pwd.sub(REGEX_TRAILING_LANG, ''), 'src', project_name.split('.'), 'Messages.java')
          if File.exist?(full)
            keys = File.readlines(full).collect{|line| m = /String\s+(\w+)\s*;/.match(line); [ project_name, m[1]] if m }.compact
          else
            puts "Skipping #{full}" if $VERBOSE
            next
          end
        else
          File.open(filename, 'r:ISO-8859-1').readlines.each do |line|
            key, value  = get_key_value(line)
            keys << [project_name, key]
          end
        end

        File.open(filename, 'w:ISO-8859-1') do |file|
          keys.each do |key|
            next unless key
            unless @@all_msgs[key]
              puts "Missing key #{key}"
              next
            end              
            value =  @@all_msgs[key][lang]
            value = @@all_msgs[key][MainLanguage] if value.empty?
            if value.empty?
              puts "no #{lang} value found for #{key}"
              next
            end
            file.puts "#{key.last}=#{value}".encode('ISO-8859-1')
          end
        end
      end
    end
  end

TremaEncoding = 'UTF-8'
Header = %(<?xml version="1.0" encoding="#{TremaEncoding}"?>
<!-- generated by #{File.basename(__FILE__)} -->
<trema xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
masterLang="de"
xsi:noNamespaceSchemaLocation="https://raw.githubusercontent.com/netceteragroup/trema-core/master/src/main/resources/trema-1.0.xsd">
</trema>
)
end

parser = Trollop::Parser.new do
  version "#{File.basename(__FILE__, '.rb')} (c) 2017 by Niklaus Giger <niklaus.giger@member.fsf.org>"
  banner <<-EOS
#{version}
License: Eclipse Public License 1.0 (EPL)
Useage: #{File.basename(__FILE__)} [-options] [directory1 directory]
  help manipulating files needed for translations
  using Cachefile        #{GoogleTranslation::CacheFileYaml}
    and SqLite3 database #{Sqlite3File}
EOS
  opt :to_csv   ,         "Create #{I18nInfo::CSV_NAME}.csv for all languages with entries for all [manifests|plugin]*.properties ", :default => false, :short => '-c'
  opt :add_missing,       "Add missing translations via Googe Translator using $HOME/google_translation_cache.csv", :default => nil, :short => '-a', :type => String
  opt :to_properties,     "Create [manifests|plugin]*.properties for all languages from #{I18nInfo::CSV_NAME}\n\n ", :default => false, :short => '-t'
  opt :gen_trema,         "obsolete: Create Trema files from content of database", :default => false, :short => '-g'
  opt :gen_csv  ,         "obsolete: Create statistics CSV files from content of database in each project", :default => false
  opt :gen_duplicates,    "obsolete: Create list of duplicated entries from content of database", :default => false, :short => '-u'
  opt :parse_messsage,    "obsolete: Parse all messages files and import their content to the database", :default => false, :short => '-p'
  opt :overview,          "obsolete: Print some statistics regardings projects, messages files, translation present in the database", :default => false, :short => '-o'
end

Options = Trollop::with_standard_exception_handling parser do
  raise Trollop::HelpNeeded if ARGV.empty? # show help screen
  parser.parse ARGV
end

# GoogleTranslation.translate_text('Gutschrift')
# GoogleTranslation.translate_text('elektronische Krankengeschichte')

i18n = I18nInfo.new(ARGV)
i18n.main_dir ||= File.expand_path(ARGV.first)
i18n.start_dir = Dir.pwd
if Options[:to_csv]
  i18n.to_csv
  exit
end
if Options[:to_properties]
  i18n.to_properties
  exit
end
i18n.parse if Options[:parse_messsage]
i18n.gen_trema if Options[:gen_trema]
i18n.gen_csv(File.expand_path(File.join(i18n.main_dir, File.basename(i18n.main_dir) + '_i18n.csv'))) if Options[:gen_csv]
i18n.gen_duplicates if Options[:gen_duplicates]
i18n.add_missing(Options[:add_missing]) if Options[:add_missing]
i18n.overview if Options[:overview]

