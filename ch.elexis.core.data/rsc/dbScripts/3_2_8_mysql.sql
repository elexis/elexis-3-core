CREATE OR REPLACE VIEW INVOICE_LIST_VIEW AS
    SELECT 
        r.id as InvoiceId,
        r.RnNummer AS InvoiceNo,
        r.rndatumvon,
        r.rndatumbis,
        CAST(r.rnstatus AS UNSIGNED) AS InvoiceState,
		CAST(r.betrag AS SIGNED) AS InvoiceTotal,
        f.patientid AS PatientId,
		k.bezeichnung1 AS PatName1,
        k.bezeichnung2 AS PatName2,
        k.geschlecht AS PatSex,
        k.geburtsdatum AS PatDob,
        f.id AS FallId,
        f.gesetz AS FallGesetz,
        f.garantID AS FallGarantId,
        f.KostentrID AS FallKostentrID,
        COUNT(z.id) AS paymentCount,
        SUM(CAST(z.betrag AS SIGNED)) AS paidAmount,
        (CAST(r.betrag AS SIGNED) - SUM(CAST(z.betrag AS SIGNED))) AS openAmount
    FROM
        (SELECT 
            *
        FROM
            rechnungen
        WHERE
            deleted = 0) r
            LEFT JOIN
        faelle f ON r.FallID = f.ID
            LEFT JOIN
        zahlungen z ON z.rechnungsID = r.id AND z.deleted = 0
            LEFT JOIN
        kontakt k ON f.PatientID = k.id
    GROUP BY r.ID;