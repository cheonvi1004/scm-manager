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

Sonia.rest.Grid = Ext.extend(Ext.grid.GridPanel, {

  urlTemplate: '<a href="{0}" target="_blank">{0}</a>',
  mailtoTemplate: '<a href="mailto: {0}">{0}</a>',
  checkboxTemplate: '<input type="checkbox" disabled="true" {0}/>',
  emptyText: 'No items available',
  minHeight: 150,

  initComponent: function(){

    var selectionModel = new Ext.grid.RowSelectionModel({
      singleSelect: true
    });

    selectionModel.on({
      selectionchange: {
        scope: this,
        fn: this.selectionChanged
      }
    });

    var config = {
      minHeight: this.minHeight,
      loadMask: true,
      sm: selectionModel,
      viewConfig: {
        deferEmptyText: false,
        emptyText: this.emptyText
      }
    };

    this.addEvents('fallBelowMinHeight');

    Ext.EventManager.onWindowResize(this.resize, this);

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.rest.Grid.superclass.initComponent.apply(this, arguments);
  },

  resize: function(){
    var h = this.getHeight();
    if (debug){
      console.debug('' + h + ' < ' + this.minHeight + " = " + (h < this.minHeight));
    }
    if ( h < this.minHeight ){
      if ( debug ){
        console.debug( 'fire event fallBelowMinHeight' );
      }
      this.fireEvent('fallBelowMinHeight', h, this.minHeight);
    }
  },

  onDestroy: function(){
    Ext.EventManager.removeResizeListener(this.resize, this);
    Sonia.rest.Grid.superclass.onDestroy.apply(this, arguments);
  },

  reload: function(callback, scope){
    if ( debug ){
      console.debug('reload store');
    }
    
    if ( Ext.isFunction(callback) ){
      this.store.load({
        callback: callback,
        scope: scope
      });
    } else {
      if (debug){
        console.debug( 'callback is not a function' );
      }
      this.store.load();
    }
  },

  selectionChanged: function(sm){
    var selected = sm.getSelected();
    if ( selected ){
      this.selectItem( selected.data );
    }
  },

  selectItem: function(item){
    if (debug){
      console.debug( item );
    }
  },

  renderUrl: function(url){
    var result = '';
    if ( url ){
      result = String.format( this.urlTemplate, url );
    }
    return result;
  },

  renderMailto: function(mail){
    var result = '';
    if ( mail ){
      result = String.format( this.mailtoTemplate, mail );
    }
    return result;
  },

  renderCheckbox: function(value){
    var param = "";
    if ( value ){
      param = "checked='checked' ";
    }
    return String.format( this.checkboxTemplate, param );
  },
  
  selectById: function(id){
    if (debug){
      console.debug( 'select by id ' + id );
    }
    var index = this.getStore().indexOfId(id);
    if ( index >= 0 ){
      this.getSelectionModel().selectRow(index);
    } else if (debug) {
      console.debug('could not find item with id ' + id);
    }
  },
  
  handleHistory: function(params){    
    if (params && params.length > 0){
      this.selectById(params[0]);
    } else {
      if (debug){
        console.debug( 'clear selection' );
      }
      this.getSelectionModel().clearSelections();
    }
  }

});

