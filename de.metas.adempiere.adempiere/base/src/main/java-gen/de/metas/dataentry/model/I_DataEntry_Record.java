package de.metas.dataentry.model;


/** Generated Interface for DataEntry_Record
 *  @author Adempiere (generated) 
 */
@SuppressWarnings("javadoc")
public interface I_DataEntry_Record 
{

    /** TableName=DataEntry_Record */
    public static final String Table_Name = "DataEntry_Record";

    /** AD_Table_ID=541169 */
//    public static final int Table_ID = org.compiere.model.MTable.getTable_ID(Table_Name);

//    org.compiere.util.KeyNamePair Model = new org.compiere.util.KeyNamePair(Table_ID, Table_Name);

    /** AccessLevel = 3 - Client - Org
     */
//    java.math.BigDecimal accessLevel = java.math.BigDecimal.valueOf(3);

    /** Load Meta Data */

	/**
	 * Get Mandant.
	 * Mandant für diese Installation.
	 *
	 * <br>Type: TableDir
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public int getAD_Client_ID();

	public org.compiere.model.I_AD_Client getAD_Client();

    /** Column definition for AD_Client_ID */
    public static final org.adempiere.model.ModelColumn<I_DataEntry_Record, org.compiere.model.I_AD_Client> COLUMN_AD_Client_ID = new org.adempiere.model.ModelColumn<I_DataEntry_Record, org.compiere.model.I_AD_Client>(I_DataEntry_Record.class, "AD_Client_ID", org.compiere.model.I_AD_Client.class);
    /** Column name AD_Client_ID */
    public static final String COLUMNNAME_AD_Client_ID = "AD_Client_ID";

	/**
	 * Set Sektion.
	 * Organisatorische Einheit des Mandanten
	 *
	 * <br>Type: TableDir
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public void setAD_Org_ID (int AD_Org_ID);

	/**
	 * Get Sektion.
	 * Organisatorische Einheit des Mandanten
	 *
	 * <br>Type: TableDir
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public int getAD_Org_ID();

	public org.compiere.model.I_AD_Org getAD_Org();

	public void setAD_Org(org.compiere.model.I_AD_Org AD_Org);

    /** Column definition for AD_Org_ID */
    public static final org.adempiere.model.ModelColumn<I_DataEntry_Record, org.compiere.model.I_AD_Org> COLUMN_AD_Org_ID = new org.adempiere.model.ModelColumn<I_DataEntry_Record, org.compiere.model.I_AD_Org>(I_DataEntry_Record.class, "AD_Org_ID", org.compiere.model.I_AD_Org.class);
    /** Column name AD_Org_ID */
    public static final String COLUMNNAME_AD_Org_ID = "AD_Org_ID";

	/**
	 * Get Erstellt.
	 * Datum, an dem dieser Eintrag erstellt wurde
	 *
	 * <br>Type: DateTime
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public java.sql.Timestamp getCreated();

    /** Column definition for Created */
    public static final org.adempiere.model.ModelColumn<I_DataEntry_Record, Object> COLUMN_Created = new org.adempiere.model.ModelColumn<I_DataEntry_Record, Object>(I_DataEntry_Record.class, "Created", null);
    /** Column name Created */
    public static final String COLUMNNAME_Created = "Created";

	/**
	 * Get Erstellt durch.
	 * Nutzer, der diesen Eintrag erstellt hat
	 *
	 * <br>Type: Table
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public int getCreatedBy();

    /** Column definition for CreatedBy */
    public static final org.adempiere.model.ModelColumn<I_DataEntry_Record, org.compiere.model.I_AD_User> COLUMN_CreatedBy = new org.adempiere.model.ModelColumn<I_DataEntry_Record, org.compiere.model.I_AD_User>(I_DataEntry_Record.class, "CreatedBy", org.compiere.model.I_AD_User.class);
    /** Column name CreatedBy */
    public static final String COLUMNNAME_CreatedBy = "CreatedBy";

	/**
	 * Set Dateneingabefeld.
	 *
	 * <br>Type: Search
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public void setDataEntry_Field_ID (int DataEntry_Field_ID);

	/**
	 * Get Dateneingabefeld.
	 *
	 * <br>Type: Search
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public int getDataEntry_Field_ID();

	public de.metas.dataentry.model.I_DataEntry_Field getDataEntry_Field();

	public void setDataEntry_Field(de.metas.dataentry.model.I_DataEntry_Field DataEntry_Field);

    /** Column definition for DataEntry_Field_ID */
    public static final org.adempiere.model.ModelColumn<I_DataEntry_Record, de.metas.dataentry.model.I_DataEntry_Field> COLUMN_DataEntry_Field_ID = new org.adempiere.model.ModelColumn<I_DataEntry_Record, de.metas.dataentry.model.I_DataEntry_Field>(I_DataEntry_Record.class, "DataEntry_Field_ID", de.metas.dataentry.model.I_DataEntry_Field.class);
    /** Column name DataEntry_Field_ID */
    public static final String COLUMNNAME_DataEntry_Field_ID = "DataEntry_Field_ID";

	/**
	 * Set DataEntry_Record.
	 *
	 * <br>Type: ID
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public void setDataEntry_Record_ID (int DataEntry_Record_ID);

	/**
	 * Get DataEntry_Record.
	 *
	 * <br>Type: ID
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public int getDataEntry_Record_ID();

    /** Column definition for DataEntry_Record_ID */
    public static final org.adempiere.model.ModelColumn<I_DataEntry_Record, Object> COLUMN_DataEntry_Record_ID = new org.adempiere.model.ModelColumn<I_DataEntry_Record, Object>(I_DataEntry_Record.class, "DataEntry_Record_ID", null);
    /** Column name DataEntry_Record_ID */
    public static final String COLUMNNAME_DataEntry_Record_ID = "DataEntry_Record_ID";

	/**
	 * Set Aktiv.
	 * Der Eintrag ist im System aktiv
	 *
	 * <br>Type: YesNo
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public void setIsActive (boolean IsActive);

	/**
	 * Get Aktiv.
	 * Der Eintrag ist im System aktiv
	 *
	 * <br>Type: YesNo
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public boolean isActive();

    /** Column definition for IsActive */
    public static final org.adempiere.model.ModelColumn<I_DataEntry_Record, Object> COLUMN_IsActive = new org.adempiere.model.ModelColumn<I_DataEntry_Record, Object>(I_DataEntry_Record.class, "IsActive", null);
    /** Column name IsActive */
    public static final String COLUMNNAME_IsActive = "IsActive";

	/**
	 * Get Aktualisiert.
	 * Datum, an dem dieser Eintrag aktualisiert wurde
	 *
	 * <br>Type: DateTime
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public java.sql.Timestamp getUpdated();

    /** Column definition for Updated */
    public static final org.adempiere.model.ModelColumn<I_DataEntry_Record, Object> COLUMN_Updated = new org.adempiere.model.ModelColumn<I_DataEntry_Record, Object>(I_DataEntry_Record.class, "Updated", null);
    /** Column name Updated */
    public static final String COLUMNNAME_Updated = "Updated";

	/**
	 * Get Aktualisiert durch.
	 * Nutzer, der diesen Eintrag aktualisiert hat
	 *
	 * <br>Type: Table
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public int getUpdatedBy();

    /** Column definition for UpdatedBy */
    public static final org.adempiere.model.ModelColumn<I_DataEntry_Record, org.compiere.model.I_AD_User> COLUMN_UpdatedBy = new org.adempiere.model.ModelColumn<I_DataEntry_Record, org.compiere.model.I_AD_User>(I_DataEntry_Record.class, "UpdatedBy", org.compiere.model.I_AD_User.class);
    /** Column name UpdatedBy */
    public static final String COLUMNNAME_UpdatedBy = "UpdatedBy";

	/**
	 * Set Datum.
	 *
	 * <br>Type: Date
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public void setValueDate (java.sql.Timestamp ValueDate);

	/**
	 * Get Datum.
	 *
	 * <br>Type: Date
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public java.sql.Timestamp getValueDate();

    /** Column definition for ValueDate */
    public static final org.adempiere.model.ModelColumn<I_DataEntry_Record, Object> COLUMN_ValueDate = new org.adempiere.model.ModelColumn<I_DataEntry_Record, Object>(I_DataEntry_Record.class, "ValueDate", null);
    /** Column name ValueDate */
    public static final String COLUMNNAME_ValueDate = "ValueDate";

	/**
	 * Set Zahlwert.
	 * Numeric Value
	 *
	 * <br>Type: Number
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public void setValueNumber (java.math.BigDecimal ValueNumber);

	/**
	 * Get Zahlwert.
	 * Numeric Value
	 *
	 * <br>Type: Number
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public java.math.BigDecimal getValueNumber();

    /** Column definition for ValueNumber */
    public static final org.adempiere.model.ModelColumn<I_DataEntry_Record, Object> COLUMN_ValueNumber = new org.adempiere.model.ModelColumn<I_DataEntry_Record, Object>(I_DataEntry_Record.class, "ValueNumber", null);
    /** Column name ValueNumber */
    public static final String COLUMNNAME_ValueNumber = "ValueNumber";

	/**
	 * Set Stringwert.
	 *
	 * <br>Type: Text
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public void setValueStr (java.lang.String ValueStr);

	/**
	 * Get Stringwert.
	 *
	 * <br>Type: Text
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public java.lang.String getValueStr();

    /** Column definition for ValueStr */
    public static final org.adempiere.model.ModelColumn<I_DataEntry_Record, Object> COLUMN_ValueStr = new org.adempiere.model.ModelColumn<I_DataEntry_Record, Object>(I_DataEntry_Record.class, "ValueStr", null);
    /** Column name ValueStr */
    public static final String COLUMNNAME_ValueStr = "ValueStr";

	/**
	 * Set Ja/Nein-Wert.
	 *
	 * <br>Type: List
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public void setValueYesNo (java.lang.String ValueYesNo);

	/**
	 * Get Ja/Nein-Wert.
	 *
	 * <br>Type: List
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public java.lang.String getValueYesNo();

    /** Column definition for ValueYesNo */
    public static final org.adempiere.model.ModelColumn<I_DataEntry_Record, Object> COLUMN_ValueYesNo = new org.adempiere.model.ModelColumn<I_DataEntry_Record, Object>(I_DataEntry_Record.class, "ValueYesNo", null);
    /** Column name ValueYesNo */
    public static final String COLUMNNAME_ValueYesNo = "ValueYesNo";
}