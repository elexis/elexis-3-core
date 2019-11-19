#!/usr/bin/env ruby
require 'bundler/inline'
puts "It may take some time for bundler/inline to install the dependencies"
gemfile do
  source 'https://rubygems.org'
  gem 'optimist'
  gem 'pry-byebug'
  gem 'test-unit'
end

begin
require 'pry'
rescue
end
require 'optimist'

StringToolConstants = 
    {
      '\1StringTool.backslash' => /(\s|\()"\\""/,
      '\1StringTool.leer' => /(\s|\()""/,
      'StringTool.space' => '" "',
      'StringTool.equals' => '"="',
      'StringTool.crlf' => /"\\r\\n"/,
      'StringTool.lf' => /"\\n"/,
      'StringTool.slash' => '"/"',
    }
@options = Optimist::options do
  version "#{File.basename(__FILE__)} (c) 2019 by Niklaus Giger <niklaus.giger@member.fsf.org>"
  banner <<-EOS
Useage: #{File.basename(__FILE__)} directory
  Converts simple strings like '', ' ', '=' to it ch.rgw.tools.StringTool equivalent, eg StringTool.empty
  in all java files in the directory and all its subdirectories
  Without any parameters it just runs some unit tests
  #{version}
EOS
end

def fix_line(line)
  found = false
  # return found, line if /"\\"/.match(line)
  StringToolConstants.each do |constant, string|
    found = true if m = line.match(string)
    line = line.gsub(string, constant)
  end
  return found, line
end

STRINGTOOL_IMPORT = "import ch.rgw.tools.StringTool;\n"

def fix_header(filename, lines)
  lines.each_with_index do |line, pos| 
     # puts "#{pos}: #{line}"
     if line.index(STRINGTOOL_IMPORT)
       puts "Found #{STRINGTOOL_IMPORT} in #{filename}" if $VERBOSE
       return
     end
     if /^@Suppress|^@Deprecated|^@Componen|XmlRootElement|^public|^class/.match(line)
       puts "Found public class at line #{pos} in #{filename}" if $VERBOSE
       lines.insert(pos, STRINGTOOL_IMPORT)
       return
     end
     
  end
end

def fix_file(filename)
  return if /ch.rgw.utility/.match(filename)
  # Some bundles do not depend on ch.rgw.utility
  return if /ch.elexis.core.logback.rocketchat|ch.elexis.core.jpa.datasource|ch.elexis.core.findings\/|ch.elexis.core.ui.icons|ch.elexis.core.document|ch.elexis.core.jpa.entities/.match(filename)
  return if /ch.elexis.core.ui.perspective|ch.elexis.core.ui.chromium/.match(filename)
  lines = IO.readlines(filename)
  new_lines = []
  hasChanges = false
  lines.each do |line|
    found, newLine = fix_line(line)
    if found
      hasChanges = found 
    end
    new_lines << newLine 
  end
  fix_header(filename, new_lines) if hasChanges
  File.open(filename, 'w+') { |f| f.write(new_lines.join("")) }
  puts "Update #{filename}" if hasChanges
end

if ARGV.size > 0
  @main_dir = File.expand_path(ARGV.first)
  raise "We expected #{ARGV.first} to be the name of an existing directory" unless File.directory?(@main_dir)
  @scriptStarted = Time.now

  @allJavaFile = Dir.glob("#{@main_dir}/**/*.java")
  puts "Handling #{@allJavaFile.size} java files" 

  @allJavaFile.each do |javafile|
    puts "Javafile #{javafile}" if $VERBOSE
    fix_file(javafile)
  end

  @scriptStopped = Time.now
  @diffSeconds = (@scriptStopped-@scriptStarted).to_i
  puts "#{Time.now}: Script finished after #{sprintf('%i:%02i', @diffSeconds/60, @diffSeconds%60)}. Reloaded #{@nr_loaded} redmine tickets"
else # built in unit test
  require 'test/unit'
  require 'test/unit/ui/console/testrunner'

  class MyTest < Test::Unit::TestCase  
    def test_leer
      assert_equal([true, '  String txt1 = p1.getText().orElse(StringTool.leer);'], fix_line(
                   '  String txt1 = p1.getText().orElse("");'));
    end
    
    def test_space
      assert_equal([true, 'int index = text.indexOf(StringTool.space);'], fix_line(
                   'int index = text.indexOf(" ");'));
    end
    def test_nothing
      assert_equal([false, 'int index = text.indexOf("Dummy");'], fix_line(
                   'int index = text.indexOf("Dummy");'));
    end
    def test_lf
      assert_equal([true, 'Collectors.joining(StringTool.lf)'], fix_line(
                   'Collectors.joining("\n")'));
    end    
    def test_backslash
      assert_equal([true, 'String repl = StringTool.backslash'], fix_line(
                   'String repl = "\""'));
    end
    def test_crlf
      assert_equal([true, 'actPat.setDiagnosen(oldDiag + StringTool.crlf + newDiag.toString());'], fix_line(
                   'actPat.setDiagnosen(oldDiag + "\r\n" + newDiag.toString());'));
    end
    def test_escacped
      assert_equal([false, 'matcher.appendReplacement(sb, "\"\"");'], fix_line(
                   'matcher.appendReplacement(sb, "\"\"");'));
    end
  end

  my_tests = Test::Unit::TestSuite.new
  my_tests << MyTest.new('test_space')
  my_tests << MyTest.new('test_leer')
  my_tests << MyTest.new('test_nothing')
  my_tests << MyTest.new('test_crlf')
  my_tests << MyTest.new('test_lf')
  my_tests << MyTest.new('test_backslash')
  my_tests << MyTest.new('test_escacped')

  #run the suite
  res = Test::Unit::UI::Console::TestRunner.run(my_tests)
  exit (res.faults.size == 0) ? 0 : 3
end

