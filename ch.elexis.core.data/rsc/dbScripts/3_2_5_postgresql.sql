DELETE FROM BEHDL_DG_JOINT
WHERE id IN (SELECT id
              FROM (SELECT id,
                             ROW_NUMBER() OVER (partition BY DiagnoseID, BehandlungsID ORDER BY id) AS rnum
                     FROM BEHDL_DG_JOINT) t
              WHERE t.rnum > 1);

ALTER TABLE BEHDL_DG_JOINT DROP CONSTRAINT behdl_dg_joint_pkey;
ALTER TABLE BEHDL_DG_JOINT
    ALTER COLUMN id TYPE character varying ;
ALTER TABLE BEHDL_DG_JOINT
    ALTER COLUMN id DROP NOT NULL;
ALTER TABLE BEHDL_DG_JOINT
    ADD CONSTRAINT primary_key PRIMARY KEY (behandlungsid, diagnoseid);