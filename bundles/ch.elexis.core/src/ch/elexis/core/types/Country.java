
package ch.elexis.core.types;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for Country.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * <p>
 *
 * <pre>
 * &lt;simpleType name="Country">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="NDF"/> <b>fallback value, not defined</b>
 *     &lt;enumeration value="AF"/>
 *     &lt;enumeration value="EG"/>
 *     &lt;enumeration value="AX"/>
 *     &lt;enumeration value="AL"/>
 *     &lt;enumeration value="DZ"/>
 *     &lt;enumeration value="AS"/>
 *     &lt;enumeration value="VI"/>
 *     &lt;enumeration value="AD"/>
 *     &lt;enumeration value="AO"/>
 *     &lt;enumeration value="AI"/>
 *     &lt;enumeration value="AQ"/>
 *     &lt;enumeration value="AG"/>
 *     &lt;enumeration value="GQ"/>
 *     &lt;enumeration value="AR"/>
 *     &lt;enumeration value="AM"/>
 *     &lt;enumeration value="AW"/>
 *     &lt;enumeration value="AC"/>
 *     &lt;enumeration value="AZ"/>
 *     &lt;enumeration value="ET"/>
 *     &lt;enumeration value="AU"/>
 *     &lt;enumeration value="BS"/>
 *     &lt;enumeration value="BH"/>
 *     &lt;enumeration value="BD"/>
 *     &lt;enumeration value="BB"/>
 *     &lt;enumeration value="BE"/>
 *     &lt;enumeration value="BZ"/>
 *     &lt;enumeration value="BJ"/>
 *     &lt;enumeration value="BM"/>
 *     &lt;enumeration value="BT"/>
 *     &lt;enumeration value="BO"/>
 *     &lt;enumeration value="BA"/>
 *     &lt;enumeration value="BW"/>
 *     &lt;enumeration value="BV"/>
 *     &lt;enumeration value="BR"/>
 *     &lt;enumeration value="BN"/>
 *     &lt;enumeration value="BG"/>
 *     &lt;enumeration value="BF"/>
 *     &lt;enumeration value="BI"/>
 *     &lt;enumeration value="CL"/>
 *     &lt;enumeration value="CN"/>
 *     &lt;enumeration value="CK"/>
 *     &lt;enumeration value="CR"/>
 *     &lt;enumeration value="CI"/>
 *     &lt;enumeration value="DK"/>
 *     &lt;enumeration value="DE"/>
 *     &lt;enumeration value="DG"/>
 *     &lt;enumeration value="DM"/>
 *     &lt;enumeration value="DO"/>
 *     &lt;enumeration value="DJ"/>
 *     &lt;enumeration value="EC"/>
 *     &lt;enumeration value="SV"/>
 *     &lt;enumeration value="ER"/>
 *     &lt;enumeration value="EE"/>
 *     &lt;enumeration value="EU"/>
 *     &lt;enumeration value="FK"/>
 *     &lt;enumeration value="FO"/>
 *     &lt;enumeration value="FJ"/>
 *     &lt;enumeration value="FI"/>
 *     &lt;enumeration value="FR"/>
 *     &lt;enumeration value="GF"/>
 *     &lt;enumeration value="PF"/>
 *     &lt;enumeration value="GA"/>
 *     &lt;enumeration value="GM"/>
 *     &lt;enumeration value="GE"/>
 *     &lt;enumeration value="GH"/>
 *     &lt;enumeration value="GI"/>
 *     &lt;enumeration value="GD"/>
 *     &lt;enumeration value="GR"/>
 *     &lt;enumeration value="GL"/>
 *     &lt;enumeration value="GB"/>
 *     &lt;enumeration value="CP"/>
 *     &lt;enumeration value="GU"/>
 *     &lt;enumeration value="GT"/>
 *     &lt;enumeration value="GG"/>
 *     &lt;enumeration value="GN"/>
 *     &lt;enumeration value="GW"/>
 *     &lt;enumeration value="GY"/>
 *     &lt;enumeration value="HT"/>
 *     &lt;enumeration value="HM"/>
 *     &lt;enumeration value="HN"/>
 *     &lt;enumeration value="HK"/>
 *     &lt;enumeration value="IN"/>
 *     &lt;enumeration value="ID"/>
 *     &lt;enumeration value="IQ"/>
 *     &lt;enumeration value="IR"/>
 *     &lt;enumeration value="IE"/>
 *     &lt;enumeration value="IS"/>
 *     &lt;enumeration value="IL"/>
 *     &lt;enumeration value="IT"/>
 *     &lt;enumeration value="JM"/>
 *     &lt;enumeration value="JP"/>
 *     &lt;enumeration value="YE"/>
 *     &lt;enumeration value="JE"/>
 *     &lt;enumeration value="JO"/>
 *     &lt;enumeration value="KY"/>
 *     &lt;enumeration value="KH"/>
 *     &lt;enumeration value="CM"/>
 *     &lt;enumeration value="CA"/>
 *     &lt;enumeration value="IC"/>
 *     &lt;enumeration value="CV"/>
 *     &lt;enumeration value="KZ"/>
 *     &lt;enumeration value="QA"/>
 *     &lt;enumeration value="KE"/>
 *     &lt;enumeration value="KG"/>
 *     &lt;enumeration value="KI"/>
 *     &lt;enumeration value="CC"/>
 *     &lt;enumeration value="CO"/>
 *     &lt;enumeration value="KM"/>
 *     &lt;enumeration value="CG"/>
 *     &lt;enumeration value="HR"/>
 *     &lt;enumeration value="CU"/>
 *     &lt;enumeration value="KW"/>
 *     &lt;enumeration value="LA"/>
 *     &lt;enumeration value="LS"/>
 *     &lt;enumeration value="LV"/>
 *     &lt;enumeration value="LB"/>
 *     &lt;enumeration value="LR"/>
 *     &lt;enumeration value="LY"/>
 *     &lt;enumeration value="LI"/>
 *     &lt;enumeration value="LT"/>
 *     &lt;enumeration value="LU"/>
 *     &lt;enumeration value="MO"/>
 *     &lt;enumeration value="MG"/>
 *     &lt;enumeration value="MW"/>
 *     &lt;enumeration value="MY"/>
 *     &lt;enumeration value="MV"/>
 *     &lt;enumeration value="ML"/>
 *     &lt;enumeration value="MT"/>
 *     &lt;enumeration value="MA"/>
 *     &lt;enumeration value="MH"/>
 *     &lt;enumeration value="MQ"/>
 *     &lt;enumeration value="MR"/>
 *     &lt;enumeration value="MU"/>
 *     &lt;enumeration value="YT"/>
 *     &lt;enumeration value="MK"/>
 *     &lt;enumeration value="MX"/>
 *     &lt;enumeration value="FM"/>
 *     &lt;enumeration value="MD"/>
 *     &lt;enumeration value="MC"/>
 *     &lt;enumeration value="MN"/>
 *     &lt;enumeration value="MS"/>
 *     &lt;enumeration value="MZ"/>
 *     &lt;enumeration value="MM"/>
 *     &lt;enumeration value="NA"/>
 *     &lt;enumeration value="NR"/>
 *     &lt;enumeration value="NP"/>
 *     &lt;enumeration value="NC"/>
 *     &lt;enumeration value="NZ"/>
 *     &lt;enumeration value="NT"/>
 *     &lt;enumeration value="NI"/>
 *     &lt;enumeration value="NL"/>
 *     &lt;enumeration value="AN"/>
 *     &lt;enumeration value="NE"/>
 *     &lt;enumeration value="NG"/>
 *     &lt;enumeration value="NU"/>
 *     &lt;enumeration value="KP"/>
 *     &lt;enumeration value="MP"/>
 *     &lt;enumeration value="NF"/>
 *     &lt;enumeration value="NO"/>
 *     &lt;enumeration value="OM"/>
 *     &lt;enumeration value="AT"/>
 *     &lt;enumeration value="PK"/>
 *     &lt;enumeration value="PS"/>
 *     &lt;enumeration value="PW"/>
 *     &lt;enumeration value="PA"/>
 *     &lt;enumeration value="PG"/>
 *     &lt;enumeration value="PY"/>
 *     &lt;enumeration value="PE"/>
 *     &lt;enumeration value="PH"/>
 *     &lt;enumeration value="PN"/>
 *     &lt;enumeration value="PL"/>
 *     &lt;enumeration value="PT"/>
 *     &lt;enumeration value="PR"/>
 *     &lt;enumeration value="RE"/>
 *     &lt;enumeration value="RW"/>
 *     &lt;enumeration value="RO"/>
 *     &lt;enumeration value="RU"/>
 *     &lt;enumeration value="SB"/>
 *     &lt;enumeration value="ZM"/>
 *     &lt;enumeration value="WS"/>
 *     &lt;enumeration value="SM"/>
 *     &lt;enumeration value="ST"/>
 *     &lt;enumeration value="SA"/>
 *     &lt;enumeration value="SE"/>
 *     &lt;enumeration value="CH"/>
 *     &lt;enumeration value="SN"/>
 *     &lt;enumeration value="CS"/>
 *     &lt;enumeration value="SC"/>
 *     &lt;enumeration value="SL"/>
 *     &lt;enumeration value="ZW"/>
 *     &lt;enumeration value="SG"/>
 *     &lt;enumeration value="SK"/>
 *     &lt;enumeration value="SI"/>
 *     &lt;enumeration value="SO"/>
 *     &lt;enumeration value="ES"/>
 *     &lt;enumeration value="LK"/>
 *     &lt;enumeration value="SH"/>
 *     &lt;enumeration value="KN"/>
 *     &lt;enumeration value="LC"/>
 *     &lt;enumeration value="PM"/>
 *     &lt;enumeration value="VC"/>
 *     &lt;enumeration value="ZA"/>
 *     &lt;enumeration value="SD"/>
 *     &lt;enumeration value="KR"/>
 *     &lt;enumeration value="SR"/>
 *     &lt;enumeration value="SJ"/>
 *     &lt;enumeration value="SZ"/>
 *     &lt;enumeration value="SY"/>
 *     &lt;enumeration value="TJ"/>
 *     &lt;enumeration value="TW"/>
 *     &lt;enumeration value="TZ"/>
 *     &lt;enumeration value="TH"/>
 *     &lt;enumeration value="TL"/>
 *     &lt;enumeration value="TG"/>
 *     &lt;enumeration value="TK"/>
 *     &lt;enumeration value="TO"/>
 *     &lt;enumeration value="TT"/>
 *     &lt;enumeration value="TA"/>
 *     &lt;enumeration value="TD"/>
 *     &lt;enumeration value="CZ"/>
 *     &lt;enumeration value="TN"/>
 *     &lt;enumeration value="TR"/>
 *     &lt;enumeration value="TM"/>
 *     &lt;enumeration value="TC"/>
 *     &lt;enumeration value="TV"/>
 *     &lt;enumeration value="UG"/>
 *     &lt;enumeration value="UA"/>
 *     &lt;enumeration value="HU"/>
 *     &lt;enumeration value="UY"/>
 *     &lt;enumeration value="UZ"/>
 *     &lt;enumeration value="VU"/>
 *     &lt;enumeration value="VA"/>
 *     &lt;enumeration value="VE"/>
 *     &lt;enumeration value="AE"/>
 *     &lt;enumeration value="US"/>
 *     &lt;enumeration value="VN"/>
 *     &lt;enumeration value="WF"/>
 *     &lt;enumeration value="CX"/>
 *     &lt;enumeration value="BY"/>
 *     &lt;enumeration value="EH"/>
 *     &lt;enumeration value="CF"/>
 *     &lt;enumeration value="CY"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 *
 */
@XmlType(name = "Country")
@XmlEnum
public enum Country {

	NDF, AF, EG, AX, AL, DZ, AS, VI, AD, AO, AI, AQ, AG, GQ, AR, AM, AW, AC, AZ, ET, AU, BS, BH, BD, BB, BE, BZ, BJ, BM,
	BT, BO, BA, BW, BV, BR, BN, BG, BF, BI, CL, CN, CK, CR, CI, DK, DE, DG, DM, DO, DJ, EC, SV, ER, EE, EU, FK, FO, FJ,
	FI, FR, GF, PF, GA, GM, GE, GH, GI, GD, GR, GL, GB, CP, GU, GT, GG, GN, GW, GY, HT, HM, HN, HK, IN, ID, IQ, IR, IE,
	IS, IL, IT, JM, JP, YE, JE, JO, KY, KH, CM, CA, IC, CV, KZ, QA, KE, KG, KI, CC, CO, KM, CG, HR, CU, KW, LA, LS, LV,
	LB, LR, LY, LI, LT, LU, MO, MG, MW, MY, MV, ML, MT, MA, MH, MQ, MR, MU, YT, MK, MX, FM, MD, MC, MN, MS, MZ, MM, NA,
	NR, NP, NC, NZ, NT, NI, NL, AN, NE, NG, NU, KP, MP, NF, NO, OM, AT, PK, PS, PW, PA, PG, PY, PE, PH, PN, PL, PT, PR,
	RE, RW, RO, RU, SB, ZM, WS, SM, ST, SA, SE, CH, SN, CS, SC, SL, ZW, SG, SK, SI, SO, ES, LK, SH, KN, LC, PM, VC, ZA,
	SD, KR, SR, SJ, SZ, SY, TJ, TW, TZ, TH, TL, TG, TK, TO, TT, TA, TD, CZ, TN, TR, TM, TC, TV, UG, UA, HU, UY, UZ, VU,
	VA, VE, AE, US, VN, WF, CX, BY, EH, CF, CY;

	public String value() {
		return name();
	}

	public static Country fromValue(String v) {
		return valueOf(v);
	}
}
