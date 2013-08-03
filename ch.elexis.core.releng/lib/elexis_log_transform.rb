#!/usr/bin/env ruby

class IntLit   < Struct.new(:int)
  def eval
    int.to_i;
  end
end

class Addition < Struct.new(:left, :right) 
  def initialize(left, right)
    puts "#{self.class}.initialize #{left} #{left}"
    super
  end
  def eval
    left.eval + right.eval
  end
end

class FunCall < Struct.new(:name, :args); 
  def initialize(left, right)
    puts "#{self.class}.initialize #{left} #{left}"
    super
  end
  def eval
    p args.map { |s| s.eval }
  end
end

class Package   < Struct.new(:package, :identifier)
  def initialize(args=nil)
    puts "#{self.class}.initialize #{args}"
    super
  end

  def eval
    puts "Package.eval #{package} #{identifier}"
    identifier
  end
end

class Comment   < Struct.new(:content)
  def initialize(args=nil)
    puts "#{self.class}.initialize #{args}"
    super
  end
  def eval
    content
  end
end

class Identifier   < Struct.new(:content)
  def initialize(args=nil)
    puts "#{self.class}.initialize #{args}"
    super
  end
  def eval
    content
  end
end

class Statement   < Struct.new(:statement)
  def initialize(args=nil)
    puts "#{self.class}.initialize #{args}"
    super
  end

  def eval
    statement
  end
end

class ImportStatement   < Struct.new(:importstatement)
  def initialize(args=nil)
    puts "#{self.class}.initialize #{args}"
    super
  end
  def eval
    pp importstatement
  end
end

class Elexis_Log_Transform < Parslet::Transform
  rule(:int  => simple(:int))        { IntLit.new(int) }
  rule(:line => simple(:line))        { Comment.new(line) }
  rule(:multi => simple(:multi))        { Comment.new(multi) }
  rule(:identifier => simple(:identifier))        { Identifier.new(identifier) }
#  rule(:statement => simple(:statement))        { Statement.new(statement) }
  rule(
    :package => 'package', 
    :identifier => simple(:identifier)  { Package.new('package', identifier) }
  ) if false
#  rule(:package => simple(:package))        { Package.new(multi) }
#  rule(:importstatement => simple(:importstatement))        { ImportStatement.new(importstatement) }
  rule(
    :left => simple(:left), 
    :right => simple(:right), 
    :op => '+')                     { Addition.new(left, right) }
  rule(
    :funcall => 'puts', 
    :arglist => subtree(:arglist))  { FunCall.new('puts', arglist) }
end

