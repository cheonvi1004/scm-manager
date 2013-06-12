/*
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

Sonia.repository.ImportWindow =  Ext.extend(Ext.Window,{
  
  titleText: 'Import Repositories',
  okText: 'Ok',
  closeText: 'Close',
  
  // cache
  importForm: null,
  
  imported: [],
  importJobsFinished: 0,
  importJobs: 0,
  
  initComponent: function(){
    this.imported = [];
    this.importJobsFinished = 0;
    this.importJobs = 0;
    
    var config = {
      layout:'fit',
      width:300,
      height:170,
      closable: true,
      resizable: false,
      plain: true,
      border: false,
      modal: true,
      title: this.titleText,
      items: [{
        id: 'importRepositoryForm',
        frame: true,
        xtype: 'form',
        defaultType: 'checkbox'
      }],
      buttons: [{
        id: 'startRepositoryImportButton',
        text: this.okText,
        formBind: true,
        scope: this,
        handler: this.importRepositories
      },{
        text: this.closeText,
        scope: this,
        handler: this.close
      }],
      listeners: {
        afterrender: {
          fn: this.readImportableTypes,
          scope: this
        }
      }
    };
    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.repository.ImportWindow.superclass.initComponent.apply(this, arguments);
  },
  
  readImportableTypes: function(){
    if (debug){
      console.debug('read importable types');
    }
    
    Ext.Ajax.request({
      url: restUrl + 'import/repositories.json',
      method: 'GET',
      scope: this,
      success: function(response){
        var obj = Ext.decode(response.responseText);
        this.renderTypeCheckboxes(obj);
        this.doLayout();
      },
      failure: function(result){
        main.handleRestFailure(
          result, 
          this.errorTitleText, 
          this.errorMsgText
        );
      }
    });
    
  },
  
  renderTypeCheckboxes: function(types){
    Ext.each(types, function(type){
      this.renderCheckbox(type);
    }, this);
  },
  
  getImportForm: function(){
    if (!this.importForm){
      this.importForm = Ext.getCmp('importRepositoryForm');
    }
    return this.importForm;
  },
    
  renderCheckbox: function(type){
    this.getImportForm().add({
      xtype: 'checkbox',
      name: 'type',
      fieldLabel: type.displayName,
      inputValue: type.name
    });
  },
  
  importRepositories: function(){
    if (debug){
      console.debug('start import of repositories');
    }
    var form = this.getImportForm().getForm();
    var values = form.getValues().type;
    if ( values ){
      if ( Ext.isArray(values) ){
        this.importJobs = values.length;
      } else {
        this.importJobs = 1;
      }
    } else {
      this.importJobs = 0;
    }
    Ext.each(values, function(value){
      this.importRepositoriesOfType(value);
    }, this);
  },
  
  appendImported: function(repositories){
    for (var i=0; i<repositories.length; i++){
      this.imported.push(repositories[i]);
    }
    this.importJobsFinished++;
    if ( this.importJobsFinished >= this.importJobs ){
      if (debug){
        console.debug( 'import of ' + this.importJobsFinished + ' jobs finished'  );
      }
      this.printImported();
    }
  },
  
  printImported: function(){
    var store = new Ext.data.JsonStore({
      fields: ['type', 'name']
    });
    store.loadData(this.imported);
    
    var colModel = new Ext.grid.ColumnModel({
      defaults: {
        sortable: true,
        scope: this
      },
      columns: [
        {id: 'name', header: 'Name', dataIndex: 'name'},
        {id: 'type', header: 'Type', dataIndex: 'type'}
      ]
    });
    
    this.getImportForm().add({
      xtype: 'grid',
      autoExpandColumn: 'name',
      store: store,
      colModel: colModel,
      height: 100
    });
    var h = this.getHeight();
    this.setHeight( h + 100 );
    this.doLayout();
    
    // reload repositories panel
    var panel = Ext.getCmp('repositories');
    if (panel){
      panel.getGrid().reload();
    }
  },
  
  importRepositoriesOfType: function(type){
    if (debug){
      console.debug('start import of ' + type + ' repositories');
    }
    var b = Ext.getCmp('startRepositoryImportButton');
    if ( b ){
      b.setDisabled(true);
    }
    Ext.Ajax.request({
      url: restUrl + 'import/repositories/' + type + '.json',
      method: 'POST',
      scope: this,
      success: function(response){
        var obj = Ext.decode(response.responseText);
        this.appendImported(obj);
      },
      failure: function(result){
        main.handleRestFailure(
          result, 
          this.errorTitleText, 
          this.errorMsgText
        );
      }
    });
  }
  
});