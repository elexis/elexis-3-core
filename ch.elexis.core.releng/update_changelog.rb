#!/usr/bin/env ruby

require 'pp'
require 'pry'
require 'ostruct'
require 'rugged'
require 'trollop'

@options = Trollop::options do
  version "#{File.basename(__FILE__)} (c) by Niklaus Giger <niklaus.giger@member.fsf.org>"
  banner <<-EOS
Useage:
  if --from is given, --to must be given, too, e.g. --from=release/3.0.25 --to=release/3.1.0
  Collects all release tag, limits them to the local history, creates statistic for
  each tag and then the Changelog
  #{version}
EOS
  opt :force_tag,       "Use HEAD and force it as tag_name ", :type => String, :default => nil
  opt :changelog,       'Name of file to be written', :type => String, :default => 'Changelog'
  opt :from,            'Only a difference from the given tag', :type => String, :default => nil
  opt :to,              'Only a difference to the given tag', :type => String, :default => nil
end

require 'rugged'
MAX_ID = 999
FORCE_TAG_NUMERIC = (MAX_ID.to_s*4).to_i
TIME_FORMAT = '%Y.%m.%d'

def tag_name_to_numerical_value(tag_name)
  items = []
  if tag_name.index('.b') # beta_vesion
    # release/3.2.0.beta22 0> [3, 2, 0, 22]
    items = tag_name.split('/').last.split(/\.(beta|b)|\./).collect{|x| x.to_i if /^\d/.match(x) }.compact
  else
    items = tag_name.split('/').last.split('.').collect{|x| x.to_i}
    items.insert(-2, MAX_ID)
  end
  sprintf('%03i%03i%03i%03i', items[0], items[1], items[2], items[3]).to_i
end

TAG_INFO = Struct.new('TAG_INFO', :tag_name, :numerical, :tag, :commit_id, :parent)
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
                                  tag, tag.target_id)
  end
  if @options[:force_tag]
    tag = @repo.references["refs/heads/master"]
    @all_taginfos << TAG_INFO.new(@options[:force_tag], FORCE_TAG_NUMERIC, tag, tag.target_id)
  end
  tags_hash
rescue => error
  puts error
  binding.pry
end

def find_tags_in_current_branch
  newer_id = @repo.references["refs/heads/master"].target_id
  newer_id = @repo.head.target_id
  walker = Rugged::Walker.new(@repo)
  walker.sorting(Rugged::SORT_TOPO | Rugged::SORT_REVERSE) # optional
  walker.push(newer_id)
  res = walker.collect{|c| c};
  @tag_in_local_branch = []
  res.each do |commit|
    if (info = @all_taginfos.find{|taginfo| taginfo.commit_id == commit.oid})
      @tag_in_local_branch << info
      puts "Adding local tag #{info.tag_name} commit #{info.commit_id}"  if $VERBOSE
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

def get_history(old_id, newer_id)
  old = @repo.lookup(old_id)
  newer = @repo.lookup(newer_id)
  return get_history(newer_id, old_id) if old.time > newer.time

  info = OpenStruct.new
  walker = Rugged::Walker.new(@repo)
  # walker.sorting(Rugged::SORT_TOPO || Rugged::SORT_DATE) # optional
  walker.push(newer_id)
  walker.hide(old_id)

  info.commits = []
  info.authors = {}
  walker.each do |c|
    author = c.author[:name]
    info.commits  << "#{c.oid} #{c.author[:time]} #{sprintf('%20s', author)} #{c.message.split("\n").first}"
    info.authors[author] ||= 0
    info.authors[author] += 1
  end
  binding.pry if info.commits.size == 0
  info
end

def emit_changes(ausgabe, from=nil, to=nil)
  raise "from and to must be given" unless to && from
  from_tag =@all_taginfos.find{|x| x.tag_name == from}
  from_commit = @repo.lookup(from_tag.commit_id)
  from_date = from_commit.time.strftime(TIME_FORMAT)
  to_tag =@all_taginfos.find{|x| x.tag_name == to}
  to_commit = @repo.lookup(to_tag.commit_id)
  to_date = to_commit.time.strftime(TIME_FORMAT)

  info = get_history(from_tag.commit_id, to_tag.commit_id)
  header = []
  header << ''
  line = "#{info.commits.size} commits between #{from_tag.tag_name} (#{from_date}) and #{to_tag.tag_name} (#{to_date})"
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
  ausgabe.puts info.commits.join("\n")
end
def emit_history(filename, from=nil, to=nil)
  walk = @history
  filename += "-#{from.sub('release/','')}-#{to.sub('release/','')}" if from && to
  File.open(filename, 'w+') do |ausgabe|
    ausgabe.puts "# Generated by #{File.basename(__FILE__)} on #{Time.now.strftime(TIME_FORMAT)}"
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

# Order of next calls is necessary!
@history = []
get_release_tags
if @options[:from] && @options[:to]
  emit_history(@options[:changelog], @options[:from], @options[:to])
else
  find_tags_in_current_branch
  build_branch_history
  emit_history(@options[:changelog])
end
