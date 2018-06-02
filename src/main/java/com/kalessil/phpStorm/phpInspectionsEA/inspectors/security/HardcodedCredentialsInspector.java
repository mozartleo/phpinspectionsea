package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class HardcodedCredentialsInspector extends BasePhpInspection {
    private static final String message = "It would be better idea to not hard-code credentials but to use a managed environment instead.";

    private static final Map<String, Integer> targetFunctions = new HashMap<>();
    private static final Set<String> targetNames = new HashSet<>();
    static {
        targetFunctions.put("\\mysql_connect", 2);
        targetFunctions.put("\\mysql_pconnect", 2);
        targetFunctions.put("\\mysqli_connect", 2);
        targetFunctions.put("\\mysqli_pconnect", 2);
        targetFunctions.put("\\mssql_connect", 2);
        targetFunctions.put("\\mssql_pconnect", 2);
        targetFunctions.put("\\oci_connect", 1);
        targetFunctions.put("\\oci_pconnect", 1);
        targetFunctions.put("\\ingres_connect", 2);
        targetFunctions.put("\\ingres_pconnect", 2);
        targetFunctions.put("\\sybase_connect", 2);
        targetFunctions.put("\\sybase_pconnect", 2);
        targetFunctions.put("\\db2_connect", 2);
        targetFunctions.put("\\db2_pconnect", 2);
        targetFunctions.put("\\ibase_connect", 2);
        targetFunctions.put("\\ibase_pconnect", 2);
        targetFunctions.put("\\ftp_login", 2);
        targetFunctions.put("\\imap_open", 2);
        targetFunctions.put("\\ldap_bind", 2);
        targetFunctions.put("\\cyrus_authenticate", 7);
        targetFunctions.put("\\hash_hmac", 2);
        targetFunctions.put("\\hash_hmac_file", 2);
        targetFunctions.put("\\hash_pbkdf2", 1);
        targetFunctions.put("\\password_hash", 1);
        targetFunctions.put("\\kadm5_create_principal", 2);
        targetFunctions.put("\\kadm5_chpass_principal", 2);
        targetFunctions.put("\\kadm5_init_with_password", 3);
        targetFunctions.put("\\ssh2_auth_hostbased_file", 5);
        targetFunctions.put("\\ssh2_auth_password", 2);
        targetFunctions.put("\\openssl_decrypt", 2);
        targetFunctions.put("\\openssl_encrypt", 2);
        targetFunctions.put("\\openssl_pbkdf2", 0);
        targetFunctions.put("\\openssl_pkcs12_export_to_file", 3);
        targetFunctions.put("\\openssl_pkcs12_export", 3);
        targetFunctions.put("\\openssl_pkcs12_read", 2);
        targetFunctions.put("\\ziparchive.setPassword", 0);
        targetFunctions.put("\\ziparchive.setEncryptionName", 2);
        targetFunctions.put("\\pdo.__construct", 2);
        targetFunctions.put("\\memcached.setSaslAuthData", 1);
        targetFunctions.put("\\mongodb.authenticate", 1);
        targetFunctions.put("\\rararchive.open", 1);
        targetFunctions.put("\\stomp.__construct", 2);

        for (final String fqn : targetFunctions.keySet()) {
            final String[] split = fqn.split("\\.");
            targetNames.add(split[split.length - 1].replace("\\", ""));
        }
    }

    @NotNull
    public String getShortName() {
        return "HardcodedCredentialsInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (EAUltimateApplicationComponent.areFeaturesEnabled()) {
                    final String name = reference.getName();
                    if (name != null && targetNames.contains(name.toLowerCase())) {
                        this.analyze(reference);
                    }
                }
            }

            @Override
            public void visitPhpMethodReference(@NotNull MethodReference reference) {
                if (EAUltimateApplicationComponent.areFeaturesEnabled()) {
                    final String name = reference.getName();
                    if (name != null && targetNames.contains(name.toLowerCase())) {
                        this.analyze(reference);
                    }
                }
            }

            private void analyze(@NotNull FunctionReference reference) {
                final PsiElement[] arguments = reference.getParameters();
                if (arguments.length > 0) {
                    final PsiElement resolved = OpenapiResolveUtil.resolveReference(reference);
                    if (resolved instanceof Function) {
                        final String fqn = ((Function) resolved).getFQN().toLowerCase();
                        if (targetFunctions.containsKey(fqn)) {
                            final int neededIndex = targetFunctions.get(fqn);
                            if (arguments.length >= neededIndex + 1) {
                                /* TODO: resolve it */
                            }
                        }
                    }
                }
            }
        };
    }
}
