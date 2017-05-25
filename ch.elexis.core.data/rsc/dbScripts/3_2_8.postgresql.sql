CREATE OR REPLACE VIEW INVOICE_LIST_VIEW AS
    SELECT 
        rz.id AS InvoiceId,
        rz.RnNummer AS InvoiceNo,
        rz.rndatumvon,
        rz.rndatumbis,
        rz.InvoiceState,
        rz.InvoiceTotal,
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
                r.rndatumvon,
                r.rndatumbis,
                r.fallid,
                CAST(r.rnstatus AS NUMERIC) AS InvoiceState,
                CAST(r.betrag AS NUMERIC) AS InvoiceTotal,
                COUNT(z.id) AS paymentCount,
                SUM(CAST(z.betrag AS NUMERIC)) AS paidAmount,
                (CAST(r.betrag AS NUMERIC) - SUM(CAST(z.betrag AS NUMERIC))) AS openAmount
        FROM
            RECHNUNGEN r
        LEFT JOIN zahlungen z ON z.rechnungsID = r.id AND z.deleted = '0'
        WHERE
            r.deleted = '0'
        GROUP BY r.id) rz
            LEFT JOIN
        faelle f ON rz.FallID = f.ID
            LEFT JOIN
        kontakt k ON f.PatientID = k.id;