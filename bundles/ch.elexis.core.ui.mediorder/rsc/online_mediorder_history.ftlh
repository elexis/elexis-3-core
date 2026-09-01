<#macro timeline entries dateFormat="dd.MM.yyyy" timeFormat="HH:mm" emptyText="Keine Einträge vorhanden.">
  <#if entries?? && entries?has_content>
    <ol class="tl">
      <#list entries as entry>
        <li class="tl-item tl-item--${entry.type!'info'}">

          <div class="tl-when">
            <span class="tl-date">${formatPart(entry, "date", dateFormat)}</span>
            <span class="tl-time">${formatPart(entry, "time", timeFormat)}</span>
          </div>

          <div class="tl-marker" aria-hidden="true"><span class="tl-dot"></span></div>

          <div class="tl-content">
            <p class="tl-title">${entry.title!entry.action!''}<@orderLink target=entry /><@jsonLinks target=entry /></p>
            <#if (entry.description)?has_content>
              <p class="tl-desc">${entry.description}</p>
            </#if>
            <#if (entry.actor)?has_content>
              <p class="tl-meta">durch ${entry.actor}</p>
            </#if>
          </div>

        </li>
      </#list>
    </ol>
  <#else>
    <p class="tl-empty">${emptyText}</p>
  </#if>
</#macro>

<#--
  Link auf die Bestellung in Elexis.
  Erwartet im Modell 'orderHref' (Ziel) und optional 'orderLabel' (Bezeichnung
  der Bestellung, wird als Tooltip angezeigt). Der Link wird nicht wirklich
  geoeffnet, sondern vom LocationListener der View abgefangen (siehe
  MediorderPart), der die Bestellansicht oeffnet und die Bestellung selektiert.
-->
<#macro orderLink target label="Bestellung anzeigen">
  <#if (target.orderHref)?has_content>
    <span class="order-actions">
      <a class="order-link" href="${target.orderHref}"
         title="${(target.orderLabel)!label} in Elexis oeffnen"
         onclick="event.stopPropagation();">${label}</a>
    </span>
  </#if>
</#macro>

<#--
  Aktionen fuer die JSON-Daten eines Bestellvorgangs.
  Erwartet im Modell 'jsonCopyHref' und/oder 'jsonDownloadHref'; fehlen beide,
  wird nichts ausgegeben. Die Links werden nicht wirklich geoeffnet, sondern
  vom LocationListener der View abgefangen (siehe MediorderPart).
  stopPropagation verhindert, dass ein Klick im Kopf das Akkordeon umschaltet.
-->
<#macro jsonLinks target copyLabel="JSON kopieren" downloadLabel="JSON herunterladen">
  <#if (target.jsonCopyHref)?has_content || (target.jsonDownloadHref)?has_content>
    <span class="json-actions">
      <#if (target.jsonCopyHref)?has_content>
        <a class="json-link" href="${target.jsonCopyHref}" title="JSON in die Zwischenablage kopieren"
           onclick="event.stopPropagation();">${copyLabel}</a>
      </#if>
      <#if (target.jsonDownloadHref)?has_content>
        <a class="json-link" href="${target.jsonDownloadHref}" title="JSON als Datei speichern"
           onclick="event.stopPropagation();">${downloadLabel}</a>
      </#if>
    </span>
  </#if>
</#macro>

<#function formatPart entry part pattern>
  <#if part == "date">
    <#local label = entry.dateLabel!''>
  <#else>
    <#local label = entry.timeLabel!''>
  </#if>
  <#if label?has_content>
    <#return label>
  </#if>

  <#local ts = entry.timestamp!''>
  <#if !ts?has_content>
    <#return "">
  </#if>

  <#if ts?is_date_like>
    <#if ts?is_unknown_date_like>
      <#return ts?datetime?string(pattern)>
    </#if>
    <#return ts?string(pattern)>
  </#if>

  <#-- z.B. LocalDateTime ohne java.time-Unterstützung: nur der Rohwert -->
  <#return ts?string>
</#function>
