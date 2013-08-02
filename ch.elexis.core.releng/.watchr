# test/test.watchr
require 'autowatchr'

require ‘mynyml-redgreen’ 

Autowatchr.new(self) do |config|
  config.failing_only = false
end