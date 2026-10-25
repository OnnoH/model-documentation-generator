<#import "../dmn-template-util.ftl" as util />

<#macro listKnowledgeRequirements knowledgeRequirements>

    <#if knowledgeRequirements?has_content>
        <h3>Knowledge Requirements</h3>
        <#list knowledgeRequirements as knowledgeRequirement>
            <p>
                ${knowledgeRequirement.id}
                <@util.showDocumentation knowledgeRequirement.description />
                <#if knowledgeRequirement.requiredDecisionHref?has_content>
                    <a href="${knowledgeRequirement.requiredDecisionHref}">Decision</a>
                </#if>
                <#if knowledgeRequirement.requiredInputHref?has_content>
                    <a href="${knowledgeRequirement.requiredInputHref}">Input Data</a>
                </#if>
                <#if knowledgeRequirement.requiredKnowledgeHref?has_content>
                    <a href="${knowledgeRequirement.requiredKnowledgeHref}">Business Knowledge Model</a>
                </#if>
            </p>
        </#list>
    </#if>

</#macro>