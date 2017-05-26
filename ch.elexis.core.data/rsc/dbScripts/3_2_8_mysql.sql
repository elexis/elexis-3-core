CREATE OR REPLACE VIEW INVOICE_LIST_VIEW AS
    SELECT 
        rz.id AS InvoiceId,
        rz.RnNummer AS InvoiceNo,
        rz.rndatum,
        rz.rndatumvon,
        rz.rndatumbis,
        rz.statusdatum,
        rz.InvoiceState,
        rz.InvoiceTotal,
        rz.MandantId,
        f.patientid AS PatientId,
        k.bezeichnung1 AS PatName1,
        k.bezeichnung2 AS PatName2,
        k.geschlecht AS PatSex,
        k.geburtsdatum AS PatDob,
        f.id AS FallId,
        f.gesetz AS FallGesetz,
        f.garantID AS FallGarantId,
        f.KostentrID AS FallKostentrID,
        rz.paymentCount,
        rz.paidAmount,
        rz.openAmount
    FROM
        (SELECT 
            r.id,
                r.rnnummer,
                r.rndatum,
                r.rndatumvon,
                r.rndatumbis,
                r.statusdatum,
                r.fallid,
                r.MandantId,
                CAST(r.rnstatus AS UNSIGNED) AS InvoiceState,
                CAST(r.betrag AS SIGNED) AS InvoiceTotal,
                COUNT(z.id) AS paymentCount,
                CASE
                    WHEN COUNT(z.id) = 0 THEN 0
                    ELSE SUM(CAST(z.betrag AS SIGNED))
                END paidAmount,
                CASE
                    WHEN COUNT(z.id) = 0 THEN CAST(r.betrag AS SIGNED)
                    ELSE (CAST(r.betrag AS SIGNED) - SUM(CAST(z.betrag AS SIGNED)))
                END openAmount
        FROM
            RECHNUNGEN r
        LEFT JOIN zahlungen z ON z.rechnungsID = r.id AND z.deleted = 0
        WHERE
            r.deleted = 0
        GROUP BY r.id) rz
            LEFT JOIN
        faelle f ON rz.FallID = f.ID
            LEFT JOIN
        kontakt k ON f.PatientID = k.id;