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
require 'trollop'
require 'logger'
$stdout.sync = true

class GoogleTranslation
  @@updated_cache = false
  CacheFileCSV = File.join(Dir.home, 'google_translation_cache.csv')
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
  def self.translate_text(what, target_language='it', source_language='de')
    key = [what, target_language, source_language]
    value = @@translationCache.find{|x, y| x[0].eql?(what) && x[1].eql?(target_language) && x[2].eql?(source_language) }
    unless value
      unless ENV['TRANSLATE_API_KEY']
        puts "MISSING_KEY #{key} #{key.first.encoding}"
        return
      end
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
    puts "Saving #{@@translationCache.size} entries to #{CacheFileCSV}"
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
  at_exit do GoogleTranslation.save_cache end
end

class L10N_Cache
  CSV_HEADER_START = ['translation_key']
  CSV_HEADER_SIZE  = L10N_Cache::CSV_HEADER_START.size
  JavaLanguage = 'Java'
  LanguageViews = { 'de' => 'german',
            'fr' => 'french',
            'it' => 'italian',
            'en' => 'english',
            }

  CSV_KEYS = LanguageViews.keys + [JavaLanguage]
  TRANSLATIONS_CSV_NAME = 'translations.csv'
  Translations = Struct.new(:lang, :values)
  REGEX_TRAILING_LANG = /\.plugin$|\.(#{LanguageViews.keys.join('|')})$/
  KEY_REGEX_IN_MESSAGES = /String\s+(\w+)(\s*|.=.*)/
  EscapeBackslash = /\\([\\]+)/

  def self.get_translation(key, lang)
    self.load_cache unless defined?(@@l10nCache)
    @@l10nCache[key] ? @@l10nCache[key][lang] : ''
  end

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

  def self.set_translation(key, lang, value)
    value = value ? self.convert_to_real_utf(value.sub('\\ u00', '\\u00')) : ''
    self.load_cache unless defined?(@@l10nCache)
    @@l10nCache[key] ||= {}
    @@l10nCache[key][lang] = value
  end

  def self.load_cache(cachefile)
    @@cacheCsvFile = cachefile
    @@l10nCache = {}
    if File.exist?(@@cacheCsvFile)
      index = 0
      CSV.foreach(@@cacheCsvFile, :force_quotes => true) do |cells|
        index += 1
        if index == 1
          raise "Unexpected header #{cells.join(',')}" unless cells.eql?(CSV_HEADER_START + CSV_KEYS)
          next
        end
        CSV_KEYS.each_with_index { |lang, idx| self.set_translation(cells[0], lang, cells[idx+1]) }
      end
    end
  end

  def self.save_cache(csv_file = @@cacheCsvFile)
    puts "Saving #{@@l10nCache.size} entries to #{csv_file}"
    missing_name = csv_file.sub('.csv', '_missing.csv')
    missing = CSV.open(missing_name, "wb:UTF-8", :force_quotes => true)
    missing << (CSV_HEADER_START + CSV_KEYS)
    nr_missing = 0
    CSV.open(csv_file, "wb:UTF-8", :force_quotes => true) do |csv|
      csv <<  (CSV_HEADER_START + CSV_KEYS)
      index = 0
      @@l10nCache.each do |key, info|
        index += 1
        next unless info.is_a?(Hash)
        CSV_KEYS.each{|lang| info[lang] ||= ''}
        info[L10N_Cache::JavaLanguage] = info['en'] if info['en'] && info[L10N_Cache::JavaLanguage].empty?
        info[L10N_Cache::JavaLanguage] = info['de'] if info['de'] && info[L10N_Cache::JavaLanguage].empty?
        translations = []
        CSV_KEYS.each{|lang| translations << info[lang] }
        if translations.uniq.size == 1 && translations.first.eql?('')
          puts "No translation for #{key} present"
          missing << ([key]  + translations).flatten
          nr_missing += 1
        else
          csv << ([key]  + translations).flatten
        end
      end
    end
    puts "Wrote #{nr_missing} entries into #{missing_name}" if nr_missing > 0
  end
end

class I18nInfo
  attr_accessor :main_dir, :start_dir
  @@all_msgs      ||= {}
  @@all_projects  ||= {}
  @@msg_files_read = []
  LanguageViews = { 'de' => 'german',
            'fr' => 'french',
            'it' => 'italian',
            'en' => 'english',
            }

  Translations = Struct.new(:lang, :values)

  def self.all_msgs
    @@all_msgs
  end
  def initialize(directories)
    @@directories = []
    @gen_csv = false
    @root_dir = directories.first
    directories ||= [ ARGV && ARGV[0] ]
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
  RBE_FILE_OPTIONS_FOR_WRITE = 'w+:ASCII'
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
    key, value = get_key_value(line.chomp)
    return unless key
    key = "#{project_name}_#{key}" unless /^messages/i.match(File.basename(filename))
    L10N_Cache.set_translation(key, lang, value)
  end
  
  def parse_plug_properties(project_name, lang, propfile)
    return unless File.exist?(propfile)
    File.open(propfile, RBE_FILE_OPTIONS_FOR_READ).readlines.each do |line|
      key, value = get_key_value(line.chomp, replace_dots_by_underscore: false)
      next unless key
      next if /false/.match(key)
      key = "#{project_name}_#{key}"
      L10N_Cache.set_translation(key, lang, value)
    end
  end
  def parse_plugin_xml(project_name, filename)
    return unless File.exist?(filename)
    keys = {}
    # (?:|label=")|description="|tooltip="|name="|)%([\.\w]+)
    IO.readlines(filename).each do |line|
      if (m = /%([\.\w]+)/i.match(line.chomp))
        key = [project_name, m[1] ].join('_')
        keys[key] = ''
      end
    end;
    mf = filename.sub('plugin.xml', 'META-INF/MANIFEST.MF')
    IO.readlines(mf).each do |line|
      if (m = /:\s+%([\.\w-]+)/i.match(line.chomp))
        key = [project_name, m[1] ].join('_')
        keys[key] = ''
      end
    end;
    parse_plug_properties(project_name, L10N_Cache::JavaLanguage, filename.sub('.xml', '.properties'))
    LanguageViews.keys.each do |lang|
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
      binding.pry
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
    end
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
    L10N_Cache.load_cache(File.join(Dir.pwd, L10N_Cache::TRANSLATIONS_CSV_NAME))
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
      puts "Skipping #{project_id} #{string2translate} as no source found"      
      return
    end
    # translate_text(what, target_language='it', source_language='de')
    translated = GoogleTranslation.translate_text(string2translate, lang, source_lang)
    return unless translated
    CGI.unescapeHTML(translated)
  end

  def add_csv_to_db_texts(csv_file)
    puts "Adding missing entries for #{csv_file}"
    L10N_Cache.load_cache(csv_file)
    msgs_to_add = read_translation_csv(csv_file)
    inserts = {}
    idx = 0
    msgs_to_add.each do |tag_name, value|
      idx += 1
      puts "#{Time.now}: Analysing message #{idx} of #{msgs_to_add.size}" if idx % 500 == 0
      next unless tag_name
      tag_name = tag_name.encode('utf-8')
      # Ensure that we have a german translation (which is our default language)
      german_translation = L10N_Cache.get_translation(tag_name, 'de')
      if german_translation.size == 0
        java_translation = L10N_Cache.get_translation(tag_name, L10N_Cache::JavaLanguage)
        german_translation = add_google_translation('en', java_translation, 'de')
        L10N_Cache.set_translation(tag_name, 'de', german_translation)
      end
      L10N_Cache::CSV_KEYS.each do |lang|
        next if lang.eql?(L10N_Cache::JavaLanguage) || lang.eql?('de')
        current_translation = L10N_Cache.get_translation(tag_name, lang)
        if current_translation.size == 0
          translated = add_google_translation('de', german_translation, lang)
          puts "Adding #{translated} missing translation for #{lang} #{tag_name}" if $VERBOSE
          L10N_Cache.set_translation(tag_name, lang, translated)
          inserts[[tag_name, lang]] =   translated
          puts "#{Time.now}: Added #{inserts.size} new translations" if inserts.size % 100 == 0
        end
      end
    end
    puts "Inserted #{inserts.size} missing entries of #{msgs_to_add.size}"
    msgs_to_add
  end
  def add_missing(csv_file)
    raise "You must specify an existing CSV file" unless File.file?(csv_file)
    L10N_Cache.load_cache(csv_file)
    add_csv_to_db_texts(csv_file)
    L10N_Cache::save_cache(csv_file)
  end

  def gen_languages_csv(filename, msgs)
    L10N_Cache::save_cache(filename)
    filename
  end

  def to_csv
    @@directories = [main_dir]
    @gen_csv = true
    parse_plugin_and_messages
    L10N_Cache.save_cache
    # gen_languages_csv(File.join(start_dir, L10N_Cache::TRANSLATIONS_CSV_NAME), @@all_msgs) if gen_csv
  end


  def read_translation_csv(csv_file)
  all_msgs = {}
  return all_msgs unless File.exist?(csv_file)
  index = 0
  @languages = []
    CSV.foreach(csv_file, :force_quotes => true) do |cells|
      index += 1 
      if index == 1
        raise("#{csv_file} has invalid header #{cells}") unless cells[0..L10N_Cache::CSV_HEADER_START.size-1] == L10N_Cache::CSV_HEADER_START
        @languages = cells[L10N_Cache::CSV_HEADER_SIZE..-1]
        next
      end
      key = cells[0..L10N_Cache::CSV_HEADER_START.size-1]
      key = key.first if  L10N_Cache::CSV_HEADER_SIZE == 1
      all_msgs[key] ||= {}
      @languages.each_with_index do |lang, idx|
        all_msgs[key][lang] = "#{cells[idx + L10N_Cache::CSV_HEADER_SIZE]}"
      end
    end
    all_msgs
  end

  def generate_plugin_properties(project_name, filename)
    if project_name.eql?('ch.elexis.core.findings.templates.edit')
      puts "Skipped plugin properties for #{project_name}"
      return
    end
    puts "Generating plugin properties for #{File.expand_path(filename)}" if $VERBOSE
    plugin_key_hash = parse_plugin_xml(project_name, File.join(File.dirname(filename), 'plugin.xml')) || {}
    keys = plugin_key_hash.keys
    L10N_Cache::CSV_KEYS.each do |lang|
      lang_file = filename.sub('.properties', (lang.eql?('Java') ? '' : '_' + lang) + '.properties')
      File.open(lang_file, RBE_FILE_OPTIONS_FOR_WRITE) do |file|
        keys.sort.uniq.each do |tag_name|
          next if /_false$/.match(tag_name)
          translations =   @@all_msgs[tag_name]        
          unless translations
            puts "#{project_name}: Missing translation in #{File.basename(lang_file)} for #{tag_name}"
            emit_RBE_compatible_line(file, tag_name, '', true)
            next
          end
          lang_value = translations[lang]
          lang_value = translations[L10N_Cache::JavaLanguage] if !lang_value || lang_value.empty?
          tag2write = tag_name.sub(project_name+'_','')
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

  def to_utf(string)
    begin
      /String\s+(\w+)\s*;/.match(string)
    rescue
      string = string.encode('UTF-8', 'ISO-8859-1')
    end
    string
  end
  
  def patch_messages_java(msg_java)
    project_name =  get_project_name(File.dirname(msg_java))
    content = IO.read(msg_java)
    m = /public\s+class\s+Messages\s*{/.match(content)
    new_header = %(
import org.eclipse.osgi.util.NLS;
public class Messages extends NLS {
  public static final String BUNDLE_NAME = "#{project_name}.messages";
)
  new_init_code = %(  static { // load message values from bundle file
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }

  private Messages() {
  }
}
)
    if m
      content.sub!('}', new_init_code)
      content.sub!(m[0], new_header)
    end
    content.gsub!(/(\s*=\s*[\w\.]+)/, '')
    File.open(msg_java, 'w+') do |file|
      file.write content
    end
  end
  def to_messages_properties
    load_cache
    Dir.glob("#{main_dir}/**/Messages.java").each do |msg_java|
      project_name =  get_project_name(msg_java)
      if /base.10n/i.match(project_name)
        puts "to_messages_properties skips project #{project_name} because its name matches 10n"
      end
      keys = get_keys_from_messages_java(msg_java).sort
      next unless keys.size > 0
      # Niklaus wants to undo changes in elexis-3-base
      patch_messages_java(msg_java) unless msg_java.index('elexis-3-core')
      L10N_Cache::CSV_KEYS.each do |lang|
        lang_file = msg_java.sub('Messages', 'messages').sub('.java', (lang.eql?('Java') ? '' : '_' + lang) + '.properties')
        if msg_java.index('elexis-3-core') && !project_name.eql?('i10n') && !File.exist?(lang_file)
          puts "to_messages_properties: Skip generating #{lang} #{lang_file.sub(Dir.pwd + '/', '')}"
          break
        else
          puts "to_messages_properties: Generating #{lang} #{lang_file} using #{msg_java}" if $VERBOSE
        end
        File.open(lang_file, RBE_FILE_OPTIONS_FOR_WRITE) do |file|
          keys.each do |tag_name|
            next if /_false$/.match(tag_name)
            next if tag_name.eql?('BUNDLE_NAME')
            translations =   @@all_msgs[tag_name]        
            
            unless translations
              puts "#{project_name}: Missing translation in #{File.basename(lang_file)} for #{tag_name}"
              emit_RBE_compatible_line(file, tag_name, '')
              next
            end
            lang_value = translations[lang]
            lang_value = translations[L10N_Cache::JavaLanguage] if !lang_value || lang_value.empty?
            tag2write = tag_name.sub(project_name+'_','')
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
  end
  
  def get_keys_from_messages_java(msg_java)
    project_name =  get_project_name(main_dir)
    return [] unless project_name
    lines = File.readlines(msg_java).collect{|line| to_utf(line) }
    keys = lines.collect{|line| m = L10N_Cache::KEY_REGEX_IN_MESSAGES.match(line); m[1] if m }.compact
    puts "#{project_name}: where #{msg_java} has #{keys.size} keys" if $VERBOSE
    keys
  end
  
  def load_cache
    return if @@all_msgs.size > 0
    L10N_Cache.load_cache(File.join(Dir.pwd, L10N_Cache::TRANSLATIONS_CSV_NAME))
    @@all_msgs  = read_translation_csv(File.join(start_dir, L10N_Cache::TRANSLATIONS_CSV_NAME))
  end

  # Emit a line compatible with the Essiembre Ressource Bunde Editor of Eclipse Neon
  def emit_RBE_compatible_line(file, tag_name, lang_value, is_plugin = false)
    @last_category ||= '-'
    begin
      line = "#{tag_name} = " + lang_value.dump.gsub(/^"|"$/,'').gsub('\\\\','\\').gsub('\\"', '"').gsub('\t', '\u0009')
      if is_plugin
        category = tag_name.split('.')[0..-2].join('.')
        unless category.eql?(@last_category)
          line = "\n" + line
          @last_category = category
        end
      else
        line = "\n" + line
      end
      file.puts line
    rescue => error
      puts "#{error}: Could not write #{tag_name} #{lang_value}"
      binding.pry
    end
  end


  # TODO: Generate properties files for all languages by default, but do correct stuff in l10n.{lang}
  def to_plugin_properties
    Dir.chdir(main_dir)
    L10N_Cache.load_cache(File.join(Dir.pwd, L10N_Cache::TRANSLATIONS_CSV_NAME))
    @@all_msgs  = read_translation_csv(File.join(start_dir, L10N_Cache::TRANSLATIONS_CSV_NAME))
    all_keys = @@all_msgs.keys.collect{|x| x }.uniq
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
      files.each do |filename|
        keys = []
        next if filename.split('.').index('target')
        next if filename.split('/').index('target')
        m =  /_(..)\.properties/.match(filename)
        lang = m ?  m[1] : 'en'
        puts "to_plugin_properties: Generating #{lang} for #{filename}" if $VERBOSE
        File.open(filename, RBE_FILE_OPTIONS_FOR_READ).readlines
        msg_java =filename.sub(/\_(de|fr|it|en)/, '').sub('messages.properties', 'Messages.java')
        next if msg_java.index('/bin/')
        unless File.exist?(msg_java)
          msg_java =File.join(Dir.pwd.sub(L10N_Cache::REGEX_TRAILING_LANG, ''), 'src', project_name.split('.'), 'Messages.java').gsub("/#{lang}/", '/')
        end
        if File.exist?(msg_java)
          lines = File.readlines(msg_java).collect{|line| to_utf(line) }
          keys = lines.collect{|line| m = /String\s+(\w+)\s*;/.match(line); [ project_name, m[1]] if m }.compact
          keys += lines.collect{|line| m = /String\s+(\w+)\s*;/.match(line); [ project_name.sub(/\.#{lang}$/, ''), m[1]] if m }.compact
          if keys.size == 0
            puts "Skipping #{msg_java} which contains no keys" if $VERBOSE
            next
          end
        else
          puts "Skipping #{msg_java}" if $VERBOSE
          next
        end

        File.open(filename, RBE_FILE_OPTIONS_FOR_WRITE) do |file|
          keys.sort.uniq.each do |full_key|
            next unless full_key[1]
            tag_name, dummy =  get_key_value("#{full_key[1]}= 'dummy")
            tag_name = "#{project_name}_#{tag_name}" unless /^messages/i.match(File.basename(filename))

            if @@all_msgs[full_key]
              value =  @@all_msgs[full_key]
            else
              # search some variant
              value = @@all_msgs[  tag_name ]
              value ||= @@all_msgs[ [tag_name.sub(/./,'') ] ]
              value ||= @@all_msgs[ [tag_name.sub(/%/,'') ] ]
              unless value
                puts "Missing #{lang} translation for #{full_key.last}"
                emit_RBE_compatible_line(file, full_key, '')
                next
              end
            end
            lang_value = value[lang]
            lang_value = value[L10N_Cache::JavaLanguage] if !lang_value || lang_value.empty?
            if tag_name && (!lang_value || lang_value.empty?)
              lang_value ||= @@all_msgs[l10n_key, tag_name]
            end
            if !lang_value || lang_value.empty?
              puts "no #{lang} value found for #{full_key}"
              next
            end
            binding.pry
            emit_RBE_compatible_line(file, tag_name, lang_value)
          end
        end
      end if false
    end
  end
end

parser = Trollop::Parser.new do
  version "#{File.basename(__FILE__, '.rb')} (c) 2017 by Niklaus Giger <niklaus.giger@member.fsf.org>"
  banner <<-EOS
#{version}
License: Eclipse Public License 1.0 (EPL)
Useage: #{File.basename(__FILE__)} [-options] [directory1 directory]
  help manipulating files needed for translations
  using Cachefile        #{GoogleTranslation::CacheFileCSV} (UTF-8)
EOS
  opt :to_csv   ,         "Create #{L10N_Cache::TRANSLATIONS_CSV_NAME} for all languages with entries for all [manifests|plugin]*.properties ", :default => false, :short => '-c'
  opt :add_missing,       "Add missing translations for a given csv file via Googe Translator using $HOME/google_translation_cache.csv", :default => nil, :short => '-a', :type => String
  opt :to_plugin_properties,     "Create plugin*.properties   for all languages from #{L10N_Cache::TRANSLATIONS_CSV_NAME}", :default => false, :short => '-p'
  opt :to_messages_properties,   "Create messages*.properties for all languages from #{L10N_Cache::TRANSLATIONS_CSV_NAME}\n\n ", :default => false, :short => '-m'
end

Options = Trollop::with_standard_exception_handling parser do
  raise Trollop::HelpNeeded if ARGV.empty? # show help screen
  parser.parse ARGV
end

# GoogleTranslation.translate_text('Gutschrift')
# GoogleTranslation.translate_text('elektronische Krankengeschichte')

i18n = I18nInfo.new(ARGV)
i18n.start_dir = Dir.pwd
if ARGV.size > 0
  i18n.main_dir = File.expand_path(ARGV.first)
  raise "We expected #{ARGV.first} to be the name of an existing directory" unless File.directory?(i18n.main_dir)
end
i18n.main_dir ||= Dir.pwd
i18n.to_csv if Options[:to_csv]
i18n.to_messages_properties  if Options[:to_messages_properties]
i18n.to_plugin_properties if Options[:to_plugin_properties]
i18n.add_missing(Options[:add_missing]) if Options[:add_missing]

