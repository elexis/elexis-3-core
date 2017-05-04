#!/usr/bin/env ruby

require 'pp'
require 'pry'
require 'ostruct'
require 'rugged'
require 'trollop'

@options = Trollop::options do
  version "#{File.basename(__FILE__)} (c) by Niklaus Giger <niklaus.giger@member.fsf.org>"
  banner <<-EOS
  #{version}
Useage:
  * update the ChangeLog.txt
EOS
  opt :force_tag,       "Use HEAD and force it as tag_name ", :type => String, :default => nil
  opt :changelog,       'Name of file to be written', :type => String, :default => 'Changelog'
end

require 'rugged'
MAX_ID = 999
FORCE_TAG_NUMERIC = (MAX_ID.to_s*4).to_i
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

def get_release_tags
  @numeric_tags = {}
  @repo = Rugged::Repository.new(Dir.pwd)
  tags = @repo.tags.find_all{|x| x.name.index('release/')}
  tags_hash = {}
  tags.each do |tag|
    next unless /^release/i.match(tag.name)
    next if /alpha/i.match(tag.name)
    @numeric_tags[tag_name_to_numerical_value(tag.name)] = tag
    tags_hash[tag.name] = tag.target_id
  end
  if @options[:force_tag]
    tag = @repo.references["refs/heads/master"]
    @numeric_tags[FORCE_TAG_NUMERIC] = tag
  end
  tags_hash
rescue => error
  puts error
  binding.pry
end

def find_release_tags
  @ids_2_tag = {}
  puts "Numerically sorted IDs are #{@numeric_tags.sort.collect{|x| x.first}.join(',')}" if $VERBOSE
  @numeric_tags.sort.each_with_index do |tag, idx|
    info = tag.last
    tag_name = (FORCE_TAG_NUMERIC == tag.first) ? @options[:force_tag] : info.name
    commit_id = info.target_id
    @ids_2_tag[commit_id] = tag_name
  end
end

def find_tags_in_current_branch
  newer_id = @repo.references["refs/heads/master"].target_id
  newer_id = @repo.head.target_id
  walker = Rugged::Walker.new(@repo)
  walker.sorting(Rugged::SORT_TOPO | Rugged::SORT_REVERSE) # optional
  walker.push(newer_id)
  res = walker.collect{|c| c};
  @tag_ids_in_local_branch = {}
  res.each do |commit|
    if (tag = @ids_2_tag[commit.oid])
      value = @numeric_tags.find{ |num, info| info.target_id.eql?(commit.oid) }.first
      puts "Adding local tag #{tag} #{value} for commit #{commit.oid}"  if $VERBOSE
      @tag_ids_in_local_branch[commit.oid] = [ tag, value]
    end
  end
end

def build_branch_history
  @history = []
  previous = nil
  sorted = @tag_ids_in_local_branch.sort_by { |key, value| value.last }
  puts "Sorted are #{sorted.collect{ |x| x.last}.join(',')}"  if $VERBOSE
  sorted.each_with_index do |tag, idx|
    current = OpenStruct.new
    info = tag.last
    tag_name = info.first
    info = @numeric_tags[tag_name_to_numerical_value(tag_name)]
    if info
      current.commit_id = info.target_id
      commit = @repo.lookup(info.target_id)
    else
      commit =  @repo.lookup(tag.first) # master
      current.commit_id = @repo.lookup(tag.first).oid
    end
    # binding.pry unless commit && defined? commit.name
    current.tag_name = (FORCE_TAG_NUMERIC == tag.first) ? @options[:force_tag] : tag_name
    current.subject = commit.message.strip
    current.author = commit.author[:name]
    current.date = commit.time.strftime('%Y.%m.%d')
    @ids_2_tag[current.commit_id] = current.tag_name
    unless previous
      previous = current
      next
    end
    @history = current
    current.parent = previous.clone
    previous = current.clone
  end
end

def show_history(old_id, newer_id)
  #  "git log --pretty=format:'%H - %an, %ad : %s' #{tag_prev}..#{tag_cur}"
  binding.pry unless old_id && newer_id
  old = @repo.lookup(old_id)
  newer = @repo.lookup(newer_id)
  return show_history(newer_id, old_id) if old.time > newer.time

  info = OpenStruct.new
  walker = Rugged::Walker.new(@repo)
  walker.sorting(Rugged::SORT_TOPO) # optional
  walker.push(newer_id)
  walker.hide(old_id)

  info.commits = []
  info.authors = {}
  walker.each do |c|
    author = c.author[:name]
    info.commits  << "#{c.tree_id} #{sprintf('%20s', author)} #{c.time} #{c.message.split("\n").first}"
    info.authors[author] ||= 0
    info.authors[author] += 1
  end
  binding.pry if info.commits.size == 0
  info
end

def emit_history(filename)
  walk = @history
  File.open(filename, 'w+') do |ausgabe|
    while walk && walk.parent
      unless @tag_ids_in_local_branch[walk.parent.commit_id]
        walk = walk.parent
        next
      end
      old_name = walk.parent.tag_name.sub('release/', '')
      new_name = walk.tag_name.sub('release/', '')
      info = show_history( walk.commit_id, walk.parent.commit_id)
      header = []
      header << ''
      line = "#{info.commits.size} commits between #{old_name} (#{walk.parent.date}) and #{new_name} (#{walk.date})"
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
      walk = walk.parent
    end
  end
end

# Order of next calls is necessary!
get_release_tags
find_release_tags
find_tags_in_current_branch
build_branch_history
emit_history(@options[:changelog])
