require 'spec_helper'
require 'java_parser'
require 'elexis_log_transform'
require 'parslet/convenience'

describe 'Result of a Parslet#parse' do
  include Parslet; extend Parslet

  before :all do
    @j_parser = Java_Parser.new
    @j_transf = Elexis_Log_Transform.new
  end
  
  describe "puts" do
    it "should evaluate puts" do
      ast = @j_transf.apply(  @j_parser.parse( 'puts(1,2,3, 4+5)' ) )              
      ast.eval.should == [1, 2, 3, 9]
    end
  end
  
  describe 'comments' do
    it "should parse a line comment" do
      comment = '// a comment' 
      ast = @j_transf.apply( @j_parser.parse( comment ) )              
      ast.eval.should == comment
    end

    it "should parse a multi line comment" do
      comment = %(/* Comment
       *
       * and here
       */)
      parsed = @j_parser.parse( comment)
      ast = @j_transf.apply( @j_parser.parse( comment) )              
      ast.eval.should == comment
    end
  end
  
  describe 'simple identifier' do
    it 'should able identifier' do
      line = 'an_identifier'
      parsed = @j_parser.parse_with_debug( line)
      ast = @j_transf.apply( @j_parser.parse( line) )              
      ast.eval.should == line
    end
  end
  describe 'simple statements' do
    it 'should able to initialize an int' do
      line = 'int j;'
      parsed = @j_parser.parse_with_debug( line)
      ast = @j_transf.apply( @j_parser.parse( line) )              
      ast.eval.should == line
      line = 'int j = 0;'
      parsed = @j_parser.parse_with_debug( line)
      ast = @j_transf.apply( @j_parser.parse( line) )              
      ast.eval.should == line
    end
  end
  
  describe 'package' do
    it 'should parse a package definition' do
      name = 'ch.elexis.core.ui.scripting'
      name = 'ch.elexis'
      line = "package #{name};"
      parsed = @j_parser.parse_with_debug( line)
      ast = @j_transf.apply( @j_parser.parse( line) )              
      ast.eval.should == name
    end
  end
  
  describe 'Util.java' do
    it 'should parse Util.java' do
      tstFile = '/opt/elexis-3-core-ngiger/ch.elexis.core.ui/src/ch/elexis/core/ui/scripting/Util.java'
      content = IO.read(tstFile)
      parsed = @j_parser.parse_with_debug( content)
      ast = @j_transf.apply( @j_parser.parse( content) )              
      ast.eval.should == content
    end
  end if false

end