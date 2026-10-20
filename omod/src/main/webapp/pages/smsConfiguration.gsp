<%
    ui.decorateWith("appui", "standardEmrPage")
%>

<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "System Administration", link: '/' + OPENMRS_CONTEXT_PATH + '/admin/index.htm' },
        { label: "SMS Vendor Configuration" }
    ];

    jq(function () {
        function showSelectedVendor() {
            var selected = jq('input[name="gateway"]:checked').val();
            jq('.vendor-panel').hide();
            jq('#vendor-' + selected).show();
        }
        jq('input[name="gateway"]').on('change', showSelectedVendor);
        showSelectedVendor();
    });
</script>

<style>
    .sms-config { max-width: 980px; }
    .sms-config .card { background: #fff; border: 1px solid #ddd; border-radius: 3px; margin: 18px 0; padding: 20px; }
    .sms-config .provider-choice { display: flex; gap: 16px; margin: 12px 0 20px; }
    .sms-config .provider-option { border: 1px solid #ccc; border-radius: 3px; cursor: pointer; flex: 1; padding: 14px; }
    .sms-config .provider-option input { margin-right: 8px; }
    .sms-config .field { margin-bottom: 14px; }
    .sms-config .field label { display: block; font-weight: bold; margin-bottom: 5px; }
    .sms-config .field input, .sms-config .field select, .sms-config .field textarea { box-sizing: border-box; max-width: 680px; width: 100%; }
    .sms-config .field textarea { min-height: 90px; }
    .sms-config .help { color: #666; display: block; font-size: 0.9em; margin-top: 4px; }
    .sms-config .notice { border-radius: 3px; margin: 15px 0; padding: 12px; }
    .sms-config .notice-success { background: #e7f5e9; border: 1px solid #8fc898; }
    .sms-config .notice-error { background: #fbe9e7; border: 1px solid #d99b94; }
    .sms-config .password-state { color: #287a36; font-size: 0.9em; margin-left: 8px; }
    .sms-config .actions { margin-top: 20px; }
</style>

<div class="sms-config">
    <h2>SMS Vendor Configuration</h2>
    <p>Choose the vendor used by scheduled reminders and use the test panel to verify its connection.</p>

    <% if (status == 'saved' || status == 'tested') { %>
        <div class="notice notice-success">${ ui.encodeHtmlContent(detail ?: 'Operation completed') }</div>
    <% } else if (status == 'error') { %>
        <div class="notice notice-error">${ ui.encodeHtmlContent(detail ?: 'Operation failed') }</div>
    <% } %>

    <form method="post" class="card">
        <input type="hidden" name="action" value="save" />
        <h3>Active vendor</h3>
        <div class="provider-choice">
            <label class="provider-option">
                <input type="radio" name="gateway" value="fdibiz" ${ activeGateway == 'fdibiz' ? 'checked="checked"' : '' } />
                <strong>FDI Biz</strong><br />Token-based JSON API
            </label>
            <label class="provider-option">
                <input type="radio" name="gateway" value="intouch" ${ activeGateway == 'intouch' ? 'checked="checked"' : '' } />
                <strong>Intouch</strong><br />High-volume form POST API
            </label>
        </div>

        <div id="vendor-fdibiz" class="vendor-panel">
            <h3>FDI Biz settings</h3>
            <div class="field">
                <label for="fdibizUsername">Username</label>
                <input id="fdibizUsername" name="fdibizUsername" type="text" value="${ ui.encodeHtmlAttribute(fdibizUsername) }" autocomplete="off" />
            </div>
            <div class="field">
                <label for="fdibizPassword">Password
                    <% if (fdibizPasswordConfigured) { %><span class="password-state">Password is configured</span><% } %>
                </label>
                <input id="fdibizPassword" name="fdibizPassword" type="password" value="" autocomplete="new-password" />
                <span class="help">Leave blank to keep the stored password.</span>
            </div>
            <div class="field">
                <label for="fdibizSender">Sender ID</label>
                <input id="fdibizSender" name="fdibizSender" type="text" value="${ ui.encodeHtmlAttribute(fdibizSender) }" />
            </div>
            <div class="field">
                <label for="fdibizAuthorizationUrl">Authorization URL</label>
                <input id="fdibizAuthorizationUrl" name="fdibizAuthorizationUrl" type="url" value="${ ui.encodeHtmlAttribute(fdibizAuthorizationUrl) }" />
            </div>
            <div class="field">
                <label for="fdibizPostUrl">Send URL</label>
                <input id="fdibizPostUrl" name="fdibizPostUrl" type="url" value="${ ui.encodeHtmlAttribute(fdibizPostUrl) }" />
            </div>
        </div>

        <div id="vendor-intouch" class="vendor-panel">
            <h3>Intouch settings</h3>
            <div class="field">
                <label for="intouchUsername">Username</label>
                <input id="intouchUsername" name="intouchUsername" type="text" value="${ ui.encodeHtmlAttribute(intouchUsername) }" autocomplete="off" />
            </div>
            <div class="field">
                <label for="intouchPassword">Password
                    <% if (intouchPasswordConfigured) { %><span class="password-state">Password is configured</span><% } %>
                </label>
                <input id="intouchPassword" name="intouchPassword" type="password" value="" autocomplete="new-password" />
                <span class="help">Leave blank to keep the stored password.</span>
            </div>
            <div class="field">
                <label for="intouchSender">Sender ID</label>
                <input id="intouchSender" name="intouchSender" type="text" value="${ ui.encodeHtmlAttribute(intouchSender) }" />
            </div>
            <div class="field">
                <label for="intouchPostUrl">Send URL</label>
                <input id="intouchPostUrl" name="intouchPostUrl" type="url" value="${ ui.encodeHtmlAttribute(intouchPostUrl) }" />
            </div>
            <div class="field">
                <label for="intouchCoding">Message coding</label>
                <input id="intouchCoding" name="intouchCoding" type="text" value="${ ui.encodeHtmlAttribute(intouchCoding) }" />
            </div>
            <div class="field">
                <label for="intouchDeliveryReportUrl">Delivery report callback URL</label>
                <input id="intouchDeliveryReportUrl" name="intouchDeliveryReportUrl" type="url" value="${ ui.encodeHtmlAttribute(intouchDeliveryReportUrl) }" />
                <span class="help">Optional. Leave blank until a delivery-report endpoint is available.</span>
            </div>
            <div class="field">
                <label for="intouchDeliveryReportLevel">Delivery report level</label>
                <select id="intouchDeliveryReportLevel" name="intouchDeliveryReportLevel">
                    <% ['1', '2', '3'].each { level -> %>
                        <option value="${ level }" ${ intouchDeliveryReportLevel == level ? 'selected="selected"' : '' }>${ level }</option>
                    <% } %>
                </select>
            </div>
        </div>

        <div class="actions">
            <button type="submit" class="confirm">Save configuration</button>
        </div>
    </form>

    <form method="post" class="card">
        <input type="hidden" name="action" value="test" />
        <h3>Send test SMS</h3>
        <p>The test uses the saved <strong>${ activeGateway == 'intouch' ? 'Intouch' : 'FDI Biz' }</strong> configuration.</p>
        <div class="field">
            <label for="testPhoneNumber">Destination phone number</label>
            <input id="testPhoneNumber" name="testPhoneNumber" type="text" maxlength="30" required="required" placeholder="250788000000" />
        </div>
        <div class="field">
            <label for="testMessage">Message</label>
            <textarea id="testMessage" name="testMessage" maxlength="1000" required="required">RBC Messaging test</textarea>
        </div>
        <div class="actions">
            <button type="submit">Send test SMS</button>
        </div>
    </form>
</div>
