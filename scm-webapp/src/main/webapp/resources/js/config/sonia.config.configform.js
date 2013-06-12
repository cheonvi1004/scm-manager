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

Sonia.config.ConfigForm = Ext.extend(Ext.form.FormPanel, {

  title: 'Config Form',
  saveButtonText: 'Save',
  resetButtontext: 'Reset',
  
  submitText: 'Submit ...',
  loadingText: 'Loading ...',
  failedText: 'Unknown Error occurred.',

  items: null,
  onSubmit: null,
  getValues: null,

  initComponent: function(){

    var config = {
      title: null,
      style: 'margin: 10px',
      trackResetOnLoad : true,
      autoScroll : true,
      border : false,
      frame : false,
      collapsible : false,
      collapsed : false,
      layoutConfig : {
        labelSeparator : ''
      },
      items : [{
        xtype : 'fieldset',
        checkboxToggle : false,
        title : this.title,
        collapsible : true,
        autoHeight : true,
        labelWidth : 140,
        buttonAlign: 'left',
        layoutConfig : {
          labelSeparator : ''
        },
        defaults: {
          width: 250
        },
        listeners: {
          render: function(){
            if ( this.onLoad && Ext.isFunction( this.onLoad ) ){
              this.onLoad(this.el);
            }
          },
          scope: this
        },
        items: this.items,
        buttons: [{
          text: this.saveButtonText,
          scope: this,
          formBind: true,
          handler: this.submitForm
        },{
          text: this.resetButtontext,
          scope: this,
          handler: function(){
            this.getForm().reset();
          }
        }]
      }]
    };

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.config.ConfigForm.superclass.initComponent.apply(this, arguments);
  },

  load: function(values){
    this.getForm().loadRecord({
      success: true,
      data: values
    });
  },

  submitForm: function(){
    var form = this.getForm();
    if ( this.onSubmit && Ext.isFunction( this.onSubmit ) ){
      this.onSubmit( form.getValues() );
    }
  }

});

Ext.reg("configForm", Sonia.config.ConfigForm);
