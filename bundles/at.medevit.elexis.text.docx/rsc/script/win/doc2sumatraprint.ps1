param(
[string]$doc_path,
[string]$printer,
[string]$tray)

cd $home\elexis\docxScript\

$pdf_save_path = $doc_path+ "temp.pdf"

$word_app = New-Object -ComObject Word.Application

    $document = $word_app.Documents.Open($doc_path)
    
    $document.SaveAs([ref] $pdf_save_path, [ref] 17)
    $document.Close()
$word_app.Quit()


if($printer -eq "default"){

& .\SumatraPDF.exe "$pdf_save_path"
Write-host "default"
} else {
write-host "$printer"
& .\SumatraPDF.exe "$pdf_save_path" -print-to `"$printer`" -print-settings bin=`"$tray`" -exit-when-done

}
