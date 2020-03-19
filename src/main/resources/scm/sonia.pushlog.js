/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
Ext.override(Sonia.repository.ChangesetViewerGrid, {
  
  getPushlogUsername: function(record){
    var pushlogUsername = null;
    var properties = record.get('properties');
    for (var i=0; i<properties.length; i++){
      var property = properties[i];
      if ( property.key === 'pushlog.username' ){
        pushlogUsername = property.value;
        break;
      }
    }
    return pushlogUsername;
  },
  
  renderChangesetMetadata: function(author, p, record){
    var authorValue = '';
    if ( author !== null ){
      authorValue = author.name;
      if ( author.mail !== null ){
        authorValue += ' ' + String.format(this.mailTemplate, author.mail);
      }
    }
    
    var pushlogUsername = this.getPushlogUsername(record);
    if ( pushlogUsername ){
      authorValue += ' (pushed by ' + pushlogUsername + ')';
    }
    
    var description = record.get('description');
    var date = record.get('date');
    if ( date !== null ){
      date = Ext.util.Format.formatTimestamp(date);
    }
    return String.format(
      this.changesetMetadataTemplate,
      description,
      authorValue,
      date
    );
  }
  
});