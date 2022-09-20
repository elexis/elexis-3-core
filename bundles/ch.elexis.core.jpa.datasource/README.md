
# Elexis DataSource implementation

This DataSource is used by both JPA and PersistentObject. It employs a Proxy DataSource to record 
sql queries to the Java Flight Recorder.


## Connection initialization

The DataSource initializes itself according to the following order (see `#DataSourceConnectionParser`  for implementation):

Left overwrites right: _TestMode > SystemProperty > Existence DemoDB > ENV > CoreHub.localCfgTestMode > SystemProperty > _Existenz DemoDB > ENV_ 

Initialization via local config is not covered here.

See https://redmine.medelexis.ch/issues/24552 for further details.