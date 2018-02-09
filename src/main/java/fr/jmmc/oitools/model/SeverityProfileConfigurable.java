/* 
 * Copyright (C) 2018 CNRS - JMMC project ( http://www.jmmc.fr )
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

import fr.jmmc.jmcs.util.ResourceUtils;
import fr.jmmc.oitools.meta.OIFitsStandard;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;

/**
 * Implementation for the configurable profile (ascii config file)
 * @author kempsc
 */
public final class SeverityProfileConfigurable extends SeverityProfile {

    /* constants */
    /** Logger associated to meta model classes */
    protected final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(SeverityProfileConfigurable.class.getName());

    /** classloader path to configuration files */
    public static final String CONF_CLASSLOADER_PATH = "fr/jmmc/oitools/resource/";
    /** wildcard symbol '*' */
    public static final String WILDCARD = "*";
    /** any failure */
    private static final RuleFailure ANY_FAILURE = new RuleFailure(null, null, null, -1, null);

    /* members */
    /** profile's matcher map keyed by rule id */
    private final Map<String, List<ProfileMatcher>> matcherMap = new HashMap<String, List<ProfileMatcher>>(32);

    SeverityProfileConfigurable(final String name) {
        super(name);
        loadConfig();
    }

    @Override
    public void defineSeverity(final RuleFailure failure, final OIFitsStandard std) {
        List<ProfileMatcher> matchers = getMatchers(failure.getRule().name());

        if (matchers == null || !applyMatchers(matchers, failure, std)) {
            matchers = getMatchers(WILDCARD);
            applyMatchers(matchers, failure, std);
        }
    }

    private boolean applyMatchers(final List<ProfileMatcher> matchers, final RuleFailure failure, final OIFitsStandard std) {
        logger.log(Level.FINE, "applyMatchers: {0}", matchers);

        Severity severity = null;

        for (ProfileMatcher matcher : matchers) {
            if (matcher.extName != null) {
                if (!matcher.extName.equals(failure.getExtName())) {
                    continue;
                }
            }
            if (matcher.member != null) {
                if (matcher.isMemberExact) {
                    if (!matcher.member.equals(failure.getMember())) {
                        continue;
                    }
                } else {
                    if (failure.getMember() == null) {
                        continue;
                    }
                    if (!failure.getMember().startsWith(matcher.member)) {
                        continue;
                    }
                }
            }
            if (matcher.std != 0) {
                if (matcher.std != (std.ordinal() + 1)) {
                    continue;
                }
            }
            if ((severity == null) || (matcher.severity.ordinal() > severity.ordinal())) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "accept matcher: {0} for {1}", new Object[]{matcher, failure});
                }
                // always increase severity
                severity = matcher.severity;
            }
        }
        if (severity != null) {
            failure.setSeverity(severity);
            return true;
        }
        return false;
    }

    private List<ProfileMatcher> getMatchers(String ruleId) {
        return matcherMap.get(ruleId);
    }

    private void addMatcher(ProfileMatcher matcher) {
        List<ProfileMatcher> list = matcherMap.get(matcher.ruleId);
        if (list == null) {
            list = new ArrayList<ProfileMatcher>(5);
            matcherMap.put(matcher.ruleId, list);
        }
        list.add(matcher);
    }

    private void loadConfig() {
        final String configFile = CONF_CLASSLOADER_PATH + "profile_" + getName() + ".conf";

        logger.log(Level.INFO, "loading config: {0}", configFile);

        // use the class loader resource resolver
        String config = ResourceUtils.readResource(configFile);

        // Strip extra whitespaces except NewLine (\n):
        config = config.replaceAll("[ \\t\\x0B\\f\\r]{2,}", " ");

        logger.log(Level.FINE, "config: {0}", config);

        for (final StringTokenizer lineTk = new StringTokenizer(config, "\n"); lineTk.hasMoreTokens();) {
            String line = lineTk.nextToken();

            if ((line == null) || (line.length() <= 1)) {
                continue;
            }

            // remove comments:
            final int pos = line.indexOf('#');
            if (pos != -1) {
                line = line.substring(0, pos);
            }
            if (line.length() <= 1) {
                continue;
            }

            logger.log(Level.FINE, "line to process: {0}", line);

            {
                final StringTokenizer tk = new StringTokenizer(line, " ");

                String severity = null;
                String ruleId = null;
                String extName = null;
                String member = null;
                String std = null;

                if (tk.hasMoreTokens()) {
                    severity = tk.nextToken();
                }
                if (tk.hasMoreTokens()) {
                    ruleId = tk.nextToken();
                }
                if (tk.hasMoreTokens()) {
                    extName = tk.nextToken();
                }
                if (tk.hasMoreTokens()) {
                    member = tk.nextToken();
                }
                if (tk.hasMoreTokens()) {
                    std = tk.nextToken();
                }
                if (ruleId == null) {
                    throw new IllegalStateException("Missing RULE_ID in profile configuration !");
                }
                addMatcher(new ProfileMatcher(severity, ruleId, extName, member, std));
            }
        }

        logger.log(Level.FINE, "matcherMap: {0}", matcherMap);

        // Ensure default rule is defined:
        List<ProfileMatcher> matchers = getMatchers(WILDCARD);

        if ((matchers == null)
                || !applyMatchers(matchers, ANY_FAILURE, null)) {
            throw new IllegalStateException("Missing default rule [" + WILDCARD + "] !");
        }
    }

    final static class ProfileMatcher {

        final Severity severity;
        final String ruleId;
        final String extName;
        final String member;
        final int std;
        final boolean isMemberExact;

        ProfileMatcher(final String severity, final String ruleId, final String extName, final String member, final String std) {
            this.severity = parseSeverity(severity);
            if (WILDCARD.equals(ruleId)) {
                this.ruleId = WILDCARD;
            } else {
                this.ruleId = Rule.valueOf(ruleId).name(); // validation
            }
            this.extName = (WILDCARD.equals(extName)) ? null : extName;
            if ((member == null) || (WILDCARD.equals(member))) {
                this.member = null;
                this.isMemberExact = false;
            } else {
                final int pos = member.indexOf(WILDCARD);
                if (pos == -1) {
                    this.member = member;
                    this.isMemberExact = true;
                } else {
                    this.member = member.substring(0, pos);
                    this.isMemberExact = false;
                }
            }
            this.std = (std != null) ? Integer.parseInt(std) : 0;
        }

        private static Severity parseSeverity(String severity) {
            if ("WARN".equals(severity)) {
                return Severity.Warning;
            }
            if ("INFO".equals(severity)) {
                return Severity.Information;
            }
            if ("ERROR".equals(severity)) {
                return Severity.Error;
            }
            if ("OFF".equals(severity)) {
                return Severity.Disabled;
            }
            throw new IllegalStateException("Unknown Severity for [" + severity + "] !");
        }

        @Override
        public String toString() {
            return "ProfileMatcher{" + "severity=" + severity + ", ruleId=" + ruleId
                    + ", extName=" + extName + ", member=" + member
                    + ", isMemberExact=" + isMemberExact + ", std=" + std + '}';
        }

    }
}
