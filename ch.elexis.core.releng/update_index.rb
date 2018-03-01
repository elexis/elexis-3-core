#!/usr/bin/env ruby
# encoding: utf-8
# Belongs to the project https://github.com/elexis/elexis-3-core/tree/master/ch.elexis.core.releng
# A copy is saved under /usr/local/bin on the srv.elexis.info
# Updates the index.html file of the Artikelstamm

require 'fileutils'
require 'pathname'

if Dir.pwd.eql?('/home/www/artikelstamm.elexis.info') ||
    File.directory?('/home/www/artikelstamm.elexis.info')
  OUTPUTFILE=File.join('/home/www/artikelstamm.elexis.info', 'index.html')
  Dir.chdir(File.dirname(OUTPUTFILE))
else
  OUTPUTFILE=File.join(Dir.pwd, 'index.html')
end

root_dir = Dir.pwd
v5_files = {}

Versions =  { 'v5' => 'v5 Dateien direkt aus oddb2xml generiert',
              }
Versions.keys.each do |version|
  v5_files[version] = Dir.glob(root_dir+"/#{version}/*/artikelstamm_*_v5.xml")
  v5_files[version].sort!{ |x,y| File.basename(y) <=> File.basename(x) }
end


header= %(
<HTML>
<HEAD>
   <meta charset='utf-8'>
 </HEAD>
<H1>artikelstamm.elexis.info on srv.elexis.info</H1>
<A HREF="documentation.html">Dokumentation</A>
<BR>           
<A HREF="datenbasis.html">Versionsvergleich und Datenbasis</A>
<BR>   
<A HREF="https://github.com/col-panic/elexis">Artikelstamm Source Code</A>
<BR>
<BR>
Für weitere Informationen: <A HREF="http://wiki.elexis.info/Installation_Elexis_3.0_OpenSource#Installation_Artikelstamm_und_Artikelstammdaten">http://wiki.elexis.info</A> oder <A HREF="http://www.medelexis.ch">www.medelexis.ch</A>
<P>
Besten Dank an unsere Sponsoren,
<ul>
<li>Dr. med Franz Marty (Sponsoring Artikelstamm Entwicklung)</li>
<li><A HREF="https://www.medelexis.ch/">Medelexis.</A> Entwicklung/Betreuung Elexis</li>
<li><A HREF="http://www.ywesee.com/">Zeno Davatz, ywesee GmbH.</A>Stellt die Daten
    <A HREF="http://pillbox.oddb.org/Datenfluss_Spital_Apotheke_Drogerie_1.png.html"/>zusammen</A> </li>
<li><A HREF="https://www.hin.ch/">HIN, Hosting oddb2xml-Daten</A></li>
<li><A HREF="http://www.zurrose.ch/">Apotheke Zur Rose (Daten zu nicht Pharma-Artikel)</A></li>
</ul>
welche uns ermöglichen, jeden Monat die aktuellsten Medikamentedaten zu beziehen!
<br>
<p> Seit Ende 2017 werden die Dateien direkt mit Hilfe <A HREF="https://github.com/ngiger/oddb2xml/blob/artikelstamm_v5/README.md">oddb2ml</A> erstellt. Alle Ursprungsdateien sind öffentlich zugänglich und der gesamte Quellcode untersteht der GPL-v3.0 oder später. Unter  <A HREF="https://github.com/ngiger/oddb2xml/blob/artikelstamm_v5/artikelstamm.md">artikelstamm readme</A> finden Sie weiter Angaben zum gebrauchten Ruby-Quellcode.
<p>
OpenSource Anwender mit Elexis 3.1 oder höher müssen jeweils die richtige Version 3 des Artikelsstamm aus den unten angebenen Link herunterladen und importieren.
</p>
Um die grossen (>10MB) Dateien runterzuladen, lohnt es sich, den Link nicht zu öffnen, sondern über das Kontext-Menü "Ziel speichern unter .." anzuwählen.
<ul>
)
footer = %(
</ul>
<p>
Da wegen dem neuen Tarmed alle uns bekannten Anwender von Elexis auf Version 3.4 umsteigen mussten, werden keine Daten für frühere Elexis-Versionen mehr erzeugt.
</HTML>
)
ausgabe=File.open(OUTPUTFILE, 'w+')
ausgabe.puts header
# require 'pry'; binding.pry

def emit_line_item(ausgabe, version, short, file, explanation)
  ausgabe.puts '<li>'
  relative =  Pathname.new(file).relative_path_from Pathname.new(Dir.pwd)
  latest = "latest_#{short}_#{version}.xml"
  latest = latest.sub('_v5', '_v3') unless short.eql?('all')
  FileUtils.rm_f(latest, :verbose => true) if File.exist?(latest)
  FileUtils.ln_s relative, latest, :verbose => true
  ausgabe.puts "<a href=\"#{relative}\"/> #{latest}</a> #{explanation}"
  ausgabe.puts '</li>'
end

Versions.sort.reverse.each do |version, explanation|

  ausgabe.puts "<li><b><a href=\"#{version}\"> #{version}</a>: Zu verwenden mit #{explanation}</b><ul>"
  if v5_files[version]
    newest =  v5_files[version].sort.reverse.first
    emit_line_item(ausgabe, version, 'all', newest, 'Vereinheitlichter Artikelstamm') if newest
  end
  ausgabe.puts '</ul></li>'
end

ausgabe.puts footer
