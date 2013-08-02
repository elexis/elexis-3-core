#!/usr/bin/env ruby
class Java_Parser < Parslet::Parser
  # Single character rules
  rule(:lparen)     { str('(') >> space? }
  rule(:rparen)     { str(')') >> space? }
  rule(:set_to)     { str('=') >> space? }
  rule(:comma)      { str(',') >> space? }
  rule(:point)      { str('.') >> space? }
  rule(:semi_colon) { str(';') >> space? }
#  rule(:startOfIdentifier) { match(/[a-z]/i).repeat(1)  >> identifier }
  rule(:prefix) { :identifier >> ['.'] }
  rule(:prefix?) { prefix.maybe }
  rule(:space)      { match('\s').repeat(1) }
  rule(:space?)     { space.maybe }
  
  rule(:lines) { line.repeat }
  rule(:line) { spaces >> expression.repeat >> newline }
  rule(:newline) { str("\n") >> str("\r").maybe }
  
#  rule(:expression) { (str('a').as(:a) >> spaces).as(:exp) }
  
  rule(:spaces) { space.repeat }
  rule(:space) { multiline_comment | line_comment | str(' ') }
  
  rule(:line_comment) { (str('//') >> (newline.absent? >> any).repeat).as(:line) }
  rule(:multiline_comment) { (str('/*') >> (str('*/').absent? >> any).repeat >> str('*/')).as(:multi) }
#  rule(:package) { str('package') >> spaces >>  identifier >> space? >> semi_colon}
  rule(:package) { str('package') >> spaces >>  identifier >> space? >> semi_colon}

  # Things
  rule(:int)                { str('int') }
  rule(:boolean)            { str('boolean') }
  rule(:char)               { str('char') }
  rule(:constant)          { match('[0-9_]').repeat(1)  >> space? }
  rule(:null)     { match('null') }
  rule(:java_private)       { str('private') }
  rule(:java_private?)      { java_private.maybe }
  rule(:java_static)        { str('static') }
  rule(:java_static?)        { java_static.maybe }
  rule(:type_attributes)    { (java_static | java_private) >> space  }
  rule(:type_attributes?)    { type_attributes.maybe }
  rule(:java_type)          { type_attributes? >> ( int | boolean | char | identifier) >> space }
  rule(:identifier)         { match(/[a-z0-9_.]/i).repeat(1,999) >> space.maybe }
  rule(:identifier?)        { identifier.maybe }
#  rule(:integer)            { match('[0-9]').repeat(1).as(:int) >> space? }
  rule(:integer)            { match('[0-9]').repeat(1).as(:int) >> space? }
#  rule(:operator)           { match('[+=]') >> space? }
  rule(:operator)           { match('[+]') >> space? }
  rule(:import_statement)   { str('import')  >> space?  >> identifier >> semi_colon}
  rule(:ident_or_const)     { ( identifier | constant | null) }

  rule(:log_statement)      { identifier >> point >> funcall >> semi_colon}
  
  # Grammar parts
#  rule(:sum)        { ident_or_const.as(:left) >> operator.as(:op) >> ident_or_const.as(:right)  >> semi_colon }
  rule(:sum)        { integer.as(:left) >> operator.as(:op) >> expression.as(:right) }
  rule(:arglist)    { expression >> (comma >> expression).repeat }  
  rule(:statement) { (str(';').absnt? >> any).repeat(1).as(:statement) }
  rule(:funcall)    { identifier.as(:funcall) >> lparen >> arglist.as(:arglist) >> rparen }
  rule(:expression) { funcall | sum | integer | identifier | identifier}
#  rule(:expression) { funcall | sum | integer }
  rule(:expression?) { expression.maybe }
  rule(:var_decl)   { ( java_type >> identifier   | identifier >> space >> identifier) >> statement.maybe >> semi_colon  }
#  rule(:java_parser) { multiline_comment | line_comment  | expression | import_statement | log_statement | var_decl | identifier  }
  rule(:java_parser) { multiline_comment | line_comment | package | expression | identifier  | var_decl }
  
  
  # Grammar parts
  root :java_parser 
#  root(:java_parser)
end

