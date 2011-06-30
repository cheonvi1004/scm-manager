/* *
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * http://bitbucket.org/sdorra/scm-manager
 * 
 */

Ext.ns("Sonia.ldap");

Sonia.ldap.ConfigPanel = Ext.extend(Sonia.config.ConfigForm, {
  
  initComponent: function(){
    
    var config = {
      title : 'LDAP Authentication',
      items : [{
        xtype : 'textfield',
        fieldLabel : 'Fullname Attribute Name',
        name : 'attribute-name-fullname',
        allowBlank : true
      },{
        xtype : 'textfield',
        fieldLabel : 'ID Attribute Name',
        name : 'attribute-name-id',
        allowBlank : true
      },{
        xtype : 'textfield',
        fieldLabel : 'Mail Attribute Name',
        name : 'attribute-name-mail',
        allowBlank : true
      },{
        xtype : 'textfield',
        fieldLabel : 'Group Attribute Name',
        name : 'attribute-name-group',
        allowBlank : true
      },{
        xtype : 'textfield',
        fieldLabel : 'Base DN',
        name : 'base-dn',
        allowBlank : true
      },{
        xtype : 'textfield',
        fieldLabel : 'Connection DN',
        name : 'connection-dn',
        allowBlank : true
      },{
        xtype : 'textfield',
        inputType: 'password',
        fieldLabel : 'Connection Password',
        name : 'connection-password',
        allowBlank : true
      },{
        xtype : 'textfield',
        fieldLabel : 'Host URL',
        name : 'host-url',
        allowBlank : true
      },{
        xtype : 'textfield',
        fieldLabel : 'Search Filter',
        name : 'search-filter',
        allowBlank : true
      },{
        xtype : 'combo',
        fieldLabel : 'Search Scope',
        name : 'search-scope',
        allowBlank : true,
        valueField: 'scope',
        displayField: 'scope',
        typeAhead: false,
        editable: false,
        triggerAction: 'all',
        mode: 'local',
        store: new Ext.data.SimpleStore({
          fields: ['scope'],
          data: [
            ['object'],
            ['one'],
            ['sub']
          ]
        })
      },{
        xtype : 'textfield',
        fieldLabel : 'Groups Unit',
        name : 'unit-groups',
        allowBlank : true
      },{
        xtype : 'textfield',
        fieldLabel : 'Groups People',
        name : 'unit-people',
        allowBlank : true
      },{
        xtpye: 'checkbox',
        fieldLabel : 'Enabled',
        name: 'enabled'
      }]
    }
    
    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.ldap.ConfigPanel.superclass.initComponent.apply(this, arguments);
  },
  
  onSubmit: function(values){
    this.el.mask('Submit ...');
    Ext.Ajax.request({
      url: restUrl + 'config/auth/ldap.json',
      method: 'POST',
      jsonData: values,
      scope: this,
      disableCaching: true,
      success: function(response){
        this.el.unmask();
      },
      failure: function(){
        this.el.unmask();
      }
    });
  },

  onLoad: function(el){
    var tid = setTimeout( function(){ el.mask('Loading ...'); }, 100);
    Ext.Ajax.request({
      url: restUrl + 'config/auth/ldap.json',
      method: 'GET',
      scope: this,
      disableCaching: true,
      success: function(response){
        var obj = Ext.decode(response.responseText);
        this.load(obj);
        clearTimeout(tid);
        el.unmask();
      },
      failure: function(){
        el.unmask();
        clearTimeout(tid);
        alert('failure');
      }
    });
  }
  
});

// register xtype
Ext.reg("ldapConfigPanel", Sonia.ldap.ConfigPanel);

// regist config panel
registerGeneralConfigPanel({
  id: 'ldapConfigPanel',
  xtype: 'ldapConfigPanel'
});
