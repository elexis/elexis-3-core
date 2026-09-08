<#import "online_mediorder_history.ftlh" as tl>
<!DOCTYPE html>
<html lang="de">
<head>
<meta charset="UTF-8">
<#-- ?no_esc: CSS enthaelt Zeichen wie ">", die sonst maskiert wuerden -->
<style>
${(css!'')?no_esc}
</style>
</head>
<body>
<#if error!false>
  <p class="tl-empty">Historie konnte nicht dargestellt werden.</p>
<#elseif sections?? && sections?has_content>
  <div class="acc">
    <#list sections as section>
      <details class="acc-item"<#if section.expanded!false> open</#if>>
        <summary class="acc-head">
          <span class="acc-chevron" aria-hidden="true"></span>
          <span class="acc-title">${section.title!''}</span>
          <#if (section.statusLabel)?has_content>
            <span class="acc-status acc-status--${section.statusType!'closed'}">${section.statusLabel}</span>
          </#if>
          <#if (section.rangeLabel)?has_content>
            <span class="acc-range">${section.rangeLabel}</span>
          </#if>
          <#if (section.summary)?has_content>
            <span class="acc-summary">${section.summary}</span>
          </#if>
        </summary>
        <div class="acc-body">
          <@tl.timeline entries=section.entries![] />
        </div>
      </details>
    </#list>
  </div>
<#else>
  <p class="tl-empty">Keine Bestellvorgänge vorhanden.</p>
</#if>
</body>
</html>
