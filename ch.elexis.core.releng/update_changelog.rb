#!/usr/bin/env ruby

require 'pp'
begin
require 'pry'
rescue LoadError
end
require 'ostruct'
require 'rugged'
require 'trollop'
require 'oga'
require 'open-uri'
require 'yaml'
require 'csv'
require 'fileutils'
require 'active_support/all'

ISSUE_URL = 'https://redmine.medelexis.ch/issues'
CACHE_FILE = File.join(Dir.home, '.cache/redmine_issues.yaml')
IGNORE_ISSUE_STATUS = /Abgewiesen|erledigt|zur*ckgestell/i

@options = Trollop::options do
  version "#{File.basename(__FILE__)} (c) 2107 by Niklaus Giger <niklaus.giger@member.fsf.org>"
  banner <<-EOS
Useage:
  if --from is given, --to must be given, too, e.g. --from=release/3.0.25 --to=release/3.1.0
  Collects all release tag, limits them to the local history, creates statistic for
  each tag and then the Changelog
  The issues fetched from #{ISSUE_URL} are cached
  in #{CACHE_FILE} as loading tickets takes a non negligeable time.
  Issues with a status not matching #{IGNORE_ISSUE_STATUS} are reloaded if
  they were not already update via the Redmine-API today.
  #{version}
EOS

  opt :force_tag,       "Use HEAD and force it as tag_name and use it a to tag", :type => String, :default => nil
  opt :with_tickets,    "Emit also information from Medelexis Redmine (needs API) ",  :default => false
  opt :changelog,       'Name of file to be written', :type => String, :default => 'Changelog'
  opt :from,            'Only a difference from the given tag', :type => String, :default => nil
  opt :to,              'Only a difference to the given tag', :type => String, :default => nil
  opt :mediawiki,       'Create Changelog.mediawiki with a summary grouped by weekyl changes/ticket'
  opt :by_week,         'Group tickets by week'
end

require 'rugged'
MAX_ID = 999
FORCE_TAG_NUMERIC = (MAX_ID.to_s*4).to_i
DATE_FORMAT = '%Y.%m.%d'
MEDIAWIKI_FILE = 'Changelog.mediawiki'
Issue = Struct.new(:id, :subject, :fixed_version, :git_version,  :status, :project, :last_api_fetch)
CommitInfo = Struct.new(:ticket, :author_date, :committer_date, :text)
class CommitInfo
  def commit_week
    date = Date.parse(committer_date)
    sprintf('%04d Woche %02d', date.year, date.cweek)
  end
end
@scriptStarted = Time.now
@options[:with_tickets] = true if @options[:mediawiki]
if @options[:with_tickets]
  @csv_file_name = "#{@options[:changelog]}.csv"
end
@options[:to] = @options[:force_tag] if  @options[:force_tag]

def tag_name_to_numerical_value(tag_name)
  items = []
  if tag_name.index('.b') # beta_vesion
    # release/3.2.0.beta22 0> [3, 2, 0, 22]
    items = tag_name.split('/').last.split(/\.(beta|b)|\./).collect{|x| x.to_i if /^\d/.match(x) }.compact
  else
    items = tag_name.split('/').last.split('.').collect{|x| x.to_i}
    items.insert(-2, MAX_ID)
  end
  if items.size == 3
    sprintf('%03i%03i%03i%03i', items[0], items[2], 0, 0).to_i
  else
    sprintf('%03i%03i%03i%03i', items[0], items[1], items[2], items[3]).to_i
  end
end

TAG_INFO = Struct.new('TAG_INFO', :tag_name, :numerical, :tag, :commit_id, :parent, :tag_date)
@all_taginfos = []

def get_taginfo_by_name(tag_name)
  @all_taginfos.find{ |taginfo| taginfo.tag_name.eql?(tag_name)}
end

def get_release_tags
  @repo = Rugged::Repository.new(Dir.pwd)
  tags = @repo.tags.find_all{|x| x.name.index('release/')}
  tags_hash = {}
  tags.each do |tag|
    next unless /^release/i.match(tag.name)
    next if /alpha/i.match(tag.name)
    tags_hash[tag.name] = tag.target_id
    @all_taginfos << TAG_INFO.new(tag.name, tag_name_to_numerical_value(tag.name),
                                  tag, tag.target_id, nil,
                                  tag.target.committer[:time].strftime(DATE_FORMAT))
  end
  if @options[:force_tag]
    info =  TAG_INFO.new(@options[:force_tag], FORCE_TAG_NUMERIC, @repo.head, @repo.head.target_id)
    info.commit_id =  @repo.head.target_id
    info.tag_date = (Date.today+1).strftime(DATE_FORMAT)
    @all_taginfos << info
  end
  branch_names = @repo.branches.find_all{ |x| /origin\/\d+\.\d+$/.match(x.name)}.collect{|x| x.name}
  branch_names.each do |name|
    tag_id = `git merge-base master #{name}`.chomp
    short_version = /[\d\.]+$/.match(name)[0]
    tag = @repo.rev_parse(tag_id)
    @all_taginfos <<  TAG_INFO.new(name, tag_name_to_numerical_value(short_version), tag, tag.oid, nil, tag.committer[:time].strftime(DATE_FORMAT))
  end
  tags_hash
rescue => error
  puts error
  puts error.backtrace.join("\n")
end

def find_tags_in_current_branch
  newer_id = @repo.references['HEAD'].target_id
  newer_id = @repo.head.target_id
  walker = Rugged::Walker.new(@repo)
  walker.sorting(Rugged::SORT_TOPO | Rugged::SORT_REVERSE) # optional
  walker.push(newer_id)
  res = walker.collect{|c| c};
  @tag_in_local_branch = []
  res.each do |commit|
    if (info = @all_taginfos.find{|taginfo| taginfo.commit_id == commit.oid})
      @tag_in_local_branch << info
      puts "Adding local tag #{info.tag_name} commit #{info.commit_id}" if $VERBOSE
    end
  end
end

def build_branch_history
  previous = nil
  sorted = @tag_in_local_branch.sort_by { |info| info.numerical}
  puts "Sorted are #{sorted.collect{ |x| x.tag_name}.join(',')}"  if $VERBOSE
  sorted.each_with_index do |current, idx|
    unless previous
      previous = current
      next
    end
    @history = current
    current.parent = previous.clone
    previous = current.clone
  end
end

def get_history(old_id, newer_id, git_version)
  old = @repo.lookup(old_id)
  newer = @repo.lookup(newer_id)
  return get_history(newer_id, old_id, git_version) if old.time > newer.time

  info = OpenStruct.new
  walker = Rugged::Walker.new(@repo)
  # walker.sorting(Rugged::SORT_TOPO || Rugged::SORT_DATE) # optional
  walker.push(newer_id)
  walker.hide(old_id)

  info.commits = []
  info.authors = {}
  walker.each do |commit|
    author = commit.author[:name]
    ticket_id = (m =  /^\[(\d*)\]/.match(commit.message)) && m[1]
    ticket = ticket_id ? read_issue(ticket_id) : Issue.new
    ticket.git_version = git_version if ticket
    line = "#{commit.oid} #{commit.author[:time]} #{sprintf('%20s', author)} #{commit.message.split("\n").first}"
    info.commits  << CommitInfo.new(ticket, commit.author[:time].strftime(DATE_FORMAT), commit.committer[:time].strftime(DATE_FORMAT), line)
    info.authors[author] ||= 0
    info.authors[author] += 1
  end
  info
end


def read_issue(id = 5760)
  @nr_loaded ||= 0
  unless ENV['REDMINE_MEDEXIS_API']
    puts "Environment variable REDMINE_MEDEXIS_API must be specified to read an issue from the Medelexis Redmine"
    raise "Missing REDMINE_MEDEXIS_API variable"
  end
  if (ti = @ticket_cache[id])
    ti.last_api_fetch ||= Date.today
    if IGNORE_ISSUE_STATUS.match(ti.status) || ti.last_api_fetch = Date.today
      puts "Skip reading ticket #{id} with status #{ti.status} dated #{ti.last_api_fetch}" if $VERBOSE
      return ti
    end
  end
  issue = Issue.new
  issue.last_api_fetch = Date.today
  content =  open("#{ISSUE_URL}/#{id}.xml",
      "User-Agent" => "Ruby/#{RUBY_VERSION}",
      "X-Redmine-API-Key" => "#{ENV['REDMINE_MEDEXIS_API']}"
    ).read
  document = Oga.parse_xml( content ) ;
  ['id', 'subject'].each do |field|
    cmd = "issue.#{field} = document.xpath('issue/#{field}').first.text"
    eval(cmd)
  end
  ['project', 'status', 'fixed_version'].each do |field|
    cmd = "value = document.xpath('issue/#{field}').first; issue.#{field} = value ? value.get('name') : nil"
    eval(cmd)
  end
  issue.subject = issue.subject[0..79] if issue.subject
  puts "Fetched ticket #{id} #{issue.status}" if $VERBOSE
  @nr_loaded += 1
  $stdout.write "\n (re-)loaded #{@nr_loaded} tickets." if @nr_loaded % 100 == 0
  $stdout.write '.'
  @ticket_cache[id] = issue
  issue
rescue OpenURI::HTTPError => error
  puts "Issue #{id} not found"
  return nil
rescue => error
  puts error
  puts error.backtrace.join("\n")
end

def emit_mediawiki_changelog(all_commits, file)
  mediawiki = File.open(file, 'w+')
  short_version = /[\d\.]+$/.match(@options[:to])[0]
  mediawiki.puts "= Changelog für Elexis #{short_version} ab #{@options[:from]} bis #{@options[:to]} ="
  mediawiki.puts ""
  mediawiki.puts "Die Zahlen in Klammern beziehen sich auf das nicht öffentlich zugängliche Ticket-System der Firma Medelexis AG"
  mediawiki.puts ""
  if  @options[:by_week]
    sorted = all_commits.sort{|left, right| left.committer_date <=> right.committer_date}
    by_week = {}
    sorted.reverse.each do |commit|
      next unless commit.ticket && commit.ticket.id
      by_week[commit.commit_week] ||= {}
      by_week[commit.commit_week] [commit.ticket.id] = commit
      by_week[commit.commit_week] [:sunday] = Date.parse(commit.committer_date).sunday
    end
    commits.each do | week, values|
      ids = values.keys.find_all{|x| x.is_a?(String)}.uniq.sort;
      mediawiki.puts "==  #{values[:sunday].strftime('%Y.%m.%d')}: #{ids.size} gelöste Tickets =="
      mediawiki.puts ""
      ids.each do |id|
        ti = values[id].ticket
        line = "* #{sprintf("%14s", "'''([https://redmine.medelexis.ch/issues/" + ti.id + ' ' + ti.id + "])'''")} #{ti.subject.gsub(/\n|\r\n/, ',').strip}"
        mediawiki.puts line
      end
      mediawiki.puts ""
    end
  else
    all_commits.find_all{|x| x.ticket.fixed_version.eql?(short_version)}.sort{|left, right| left.committer_date <=> right.committer_date}
    sorted_by_ticket  = sorted.sort{|left, right| left.ticket.id <=> right.ticket.id}
    emitted_ids = []
    sorted_by_ticket.each do|commit|
      next if emitted_ids.index(commit.ticket.id)
      line = "* #{sprintf("%14s", "'''([https://redmine.medelexis.ch/issues/" + commit.ticket.id + ' ' + commit.ticket.id + "])'''")} #{commit.ticket.subject.gsub(/\n|\r\n/, ',').strip}"
      mediawiki.puts line
      emitted_ids << commit.ticket.id
    end
  end
  mediawiki.close
end

def emit_changes(ausgabe, from=nil, to=nil)
  raise "from and to must be given" unless to && from
  from_tag =@all_taginfos.find{|x| x.tag_name == from}
  to_tag =@all_taginfos.find{|x| x.tag_name == to}
  info = get_history(from_tag.commit_id, to_tag.commit_id, to_tag.tag_name)
  header = []
  header << ''
  line = "#{info.commits.size} commits between #{from_tag.tag_name} (#{from_tag.tag_date}) and #{to_tag.tag_name} (#{to_tag.tag_date})"
  header << line
  header << '-' * line.size
  header << '   number changes by authors are'
  info.authors.sort_by{|key, value| value}.reverse.each do |author, nr_commits|
    header << "   #{sprintf("%3i", nr_commits)}: #{author}"
  end
  header << '-' * line.size

  header << ''
  puts header if $VERBOSE
  ausgabe.puts header.join("\n")
  ausgabe.puts info.commits.collect{|info| info.text}.join("\n")
  if @csv_file_name
    unless @csv_file # create header
      @csv_file = CSV.open(@csv_file_name, "wb", :encoding => 'utf-8')
      @csv_file << ['id', 'status', 'fixed_version', 'git_version','author_date', 'committer_date', 'subject', 'project', 'text']
    end
    puts "\nAdding #{sprintf('%-25s', to_tag.tag_name)}" #  to #{@csv_file_name}"
    info.commits.each do |ci|
      next unless ci.ticket && ci.ticket.id.to_i > 0
      ti = ci.ticket
      line = [ti.id, ti.status, ti.fixed_version, ti.git_version, ci.author_date, ci.committer_date,
              ti.subject.gsub(/\n|\r\n/, ',').strip, ti.project, ci.text.gsub(/\n|\r\n/, ' ').strip]
      @csv_file << line
    end
  end
end

def emit_history(filename, from = nil, to = nil)
  walk = @history
  filename += "-#{from.sub('release/','')}-#{to.sub('release/','')}" if from && to
  File.open(filename, 'w+') do |ausgabe|
    ausgabe.puts "# Generated by #{File.basename(__FILE__)} on #{Time.now.strftime(DATE_FORMAT)}"
    ausgabe.puts "# similar to git log --date=iso --pretty=format:'%H %ad %an %s' release/3.0.25..release/3.1.0"
    if from && to
      emit_changes(ausgabe, from, to)
    else
      while walk && walk.parent
        unless @tag_in_local_branch.find{|x| x.commit_id == walk.parent.commit_id}
          walk = walk.parent
          next
        end
        emit_changes(ausgabe, walk.parent.tag_name, walk.tag_name)
        walk = walk.parent
      end
    end
  end
end

@ticket_cache = File.exist?(CACHE_FILE) ? YAML.load_file(CACHE_FILE) : {}
@ticket_cache = {} unless @ticket_cache.is_a?(Hash)
@nr_loaded ||= 0
$stdout.sync = true
puts "Loaded #{@ticket_cache.size} entries from #{CACHE_FILE}"
at_exit do
  puts "Saving #{@ticket_cache.size} entries to #{CACHE_FILE}"
  FileUtils.mv(CACHE_FILE, CACHE_FILE+ '.backup', :verbose => true) if File.exist?(CACHE_FILE) && File.size(CACHE_FILE) > 100
  File.open(CACHE_FILE,'w+') do |h|
    h.write @ticket_cache.to_yaml
  end if @nr_loaded.size != @ticket_cache.size
end
# Order of next calls is necessary!
@history = []
if @options[:mediawiki]
  raise "from and to must be given" unless @options[:from] && @options[:to]
  git_directories = (Dir.glob('.git') + Dir.glob('*/.git')).collect{|x| File.expand_path(File.dirname(x)) }
  mediawiki_file = File.join(Dir.pwd, MEDIAWIKI_FILE)
  history_all = []
  git_directories.each do |repo|
    Dir.chdir(repo)
    @all_taginfos = []
    @history = []
    get_release_tags
    from_tag = @all_taginfos.find{|x| x.tag_name ==  @options[:from] }
    if from_tag
      from_id = from_tag.commit_id
    else
      from_tag ||= @repo.branches[@options[:from]]
      from_id = from_tag.target.oid
    end
    unless from_tag
      puts "Skipping #{repo} as there is no tag #{@options[:from]}"
      @repo.ref_names.find{|x| /origin\/3.5/.match(x)}
      next
    end
    to_tag =@all_taginfos.find{|x| x.tag_name ==  @options[:to]}
    info = get_history(from_id, to_tag.commit_id, to_tag.tag_name)
    history_all +=info.commits
    puts "  #{repo}: found #{info.commits.size} history_all now #{history_all.size}"
  end
  emit_mediawiki_changelog(history_all, mediawiki_file)
  puts "Created #{mediawiki_file} #{File.size(mediawiki_file)} bytes"
else
  get_release_tags
  if @options[:from] && @options[:to]
    emit_history(@options[:changelog], @options[:from], @options[:to])
  else
    find_tags_in_current_branch
    build_branch_history
    emit_history(@options[:changelog])
  end
end
puts "\nCreated #{File.expand_path(@options[:changelog])} " +
    "#{@csv_file_name ? ' and ' +  File.expand_path(@csv_file_name) : ' '} " +
    "for #{@options[:from] ? @options[:from] : 'first commit'} up to #{@options[:to] ? @options[:to] : 'HEAD'}"
@scriptStopped = Time.now
@diffSeconds = (@scriptStopped-@scriptStarted).to_i
puts "#{Time.now}: Script finished after #{sprintf('%i:%02i', @diffSeconds/60, @diffSeconds%60)}. Reloaded #{@nr_loaded} redmine tickets"
