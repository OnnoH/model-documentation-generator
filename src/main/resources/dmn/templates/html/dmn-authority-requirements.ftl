<#import "../dmn-template-util.ftl" as util />

<#macro listAuthorityRequirements authorityRequirements>

    <#if authorityRequirements?has_content>
        <h3>Authority Requirements</h3>
        <#list authorityRequirements as authorityRequirement>
            <p>
                ${authorityRequirement.id}
                <@util.showDocumentation authorityRequirement.description />
                <#if authorityRequirement.requiredDecisionHref?has_content>
                    <a href="${authorityRequirement.requiredDecisionHref}">Decision</a>
                </#if>
                <#if authorityRequirement.requiredInputHref?has_content>
                    <a href="${authorityRequirement.requiredInputHref}">Input Data</a>
                </#if>
                <#if authorityRequirement.requiredAuthorityHref?has_content>
                    <a href="${authorityRequirement.requiredAuthorityHref}">Knowledge Source</a>
                </#if>
            </p>
        </#list>
    </#if>

</#macro>