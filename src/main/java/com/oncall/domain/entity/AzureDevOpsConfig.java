package com.oncall.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Stores the Azure DevOps connection settings used by the on-call platform
 * to fetch and link work items.
 *
 * <p>Based on the tracker repository integration pattern:
 * <ul>
 *   <li>Authentication: Personal Access Token (PAT) encoded as Basic auth
 *       {@code Base64(:<PAT>)}. The PAT is stored encrypted at the column level.</li>
 *   <li>Base URL: {@code https://dev.azure.com/<org>}</li>
 *   <li>API version: {@code 7.1-preview.3} (configurable)</li>
 * </ul>
 *
 * <p>Only one active config row is expected per deployment. Multiple rows are
 * supported for multi-org scenarios (future). The service layer selects the
 * active row by {@code active = true}.
 *
 * <p>Fields match the env schema from {@code packages/api/src/env.ts} in the
 * Agilysys-Inc/tracker repository:
 * <ul>
 *   <li>AZDO_ORG    → {@link #organization}</li>
 *   <li>AZDO_PROJECT → {@link #project}</li>
 *   <li>AZDO_TEAM   → {@link #team}</li>
 *   <li>AZDO_PAT    → {@link #personalAccessToken} (encrypted)</li>
 *   <li>AZDO_API_VERSION → {@link #apiVersion}</li>
 * </ul>
 */
@Entity
@Table(name = "azure_devops_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AzureDevOpsConfig extends BaseEntity {

    /** Azure DevOps organisation name, e.g. {@code Agilysys-Inc}. */
    @NotBlank
    @Column(nullable = false)
    private String organization;

    /**
     * Azure DevOps project name, e.g. {@code MyProject}.
     * Used as the path segment in all project-scoped API calls:
     * {@code /{project}/_apis/wit/...}
     */
    @NotBlank
    @Column(nullable = false)
    private String project;

    /**
     * Optional team name within the project.
     * When set, the project scope for query APIs becomes {@code {project}/{team}}.
     */
    @Column(name = "team")
    private String team;

    /**
     * Azure DevOps Personal Access Token.
     * Encrypted at the column level in production.
     * The service layer encodes it as {@code Base64(:<PAT>)} for the
     * {@code Authorization: Basic} header following the tracker client pattern.
     */
    @NotBlank
    @Column(name = "personal_access_token", nullable = false)
    private String personalAccessToken;

    /**
     * ADO REST API version to use in the {@code api-version} query parameter.
     * Default: {@code 7.1-preview.3}.
     */
    @Column(name = "api_version", nullable = false)
    @Builder.Default
    private String apiVersion = "7.1-preview.3";

    /** When true, this config is the active integration used by the platform. */
    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    /**
     * Derived base URL: {@code https://dev.azure.com/<organization>}.
     * Computed and stored to avoid repeated string assembly at runtime.
     */
    @Column(name = "base_url", nullable = false)
    private String baseUrl;
}
