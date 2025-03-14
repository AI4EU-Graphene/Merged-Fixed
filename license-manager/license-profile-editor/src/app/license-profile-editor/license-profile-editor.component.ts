/*-
 * ===============LICENSE_START================================================
 * Acumos Apache-2.0
 * ============================================================================
 * Copyright (C) 2019 Nordix Foundation.
 * ============================================================================
 * This Acumos software file is distributed by Nordix Foundation
 * under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * This file is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ===============LICENSE_END==================================================
 */

import { Component, OnInit, ViewEncapsulation, ViewChild, Input } from '@angular/core';
import { MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';
import { LicenseProfileServiceService } from '../license-profile-service.service';
import { JsonSchemaFormComponent } from '@earlyster/angular6-json-schema-form';
import { APP_VERSION } from '../../environments/app.version';

@Component({
  selector: 'app-license-profile-editor',
  templateUrl: './license-profile-editor.component.html',
  styleUrls: ['./license-profile-editor.component.scss'],
  encapsulation: ViewEncapsulation.ShadowDom
})
export class LicenseProfileEditorComponent implements OnInit {

  appVersion = APP_VERSION.app_git_version;
  isValid = false;
  downloadType = 'txt';
  errors: string[];
  profileFormInput: any;
  licenseProfileForm: JsonSchemaFormComponent;
  queryParams: any = {};
  title = 'Create New License Profile';

  @Input() mode: string;

  constructor(private matIconRegistry: MatIconRegistry,
              private domSanitizer: DomSanitizer,
              private service: LicenseProfileServiceService) {
    this.matIconRegistry.addSvgIcon(
      `close`,
      this.domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/close.svg')
    );
  }

  /**
   * JavaScript Get URL Parameter parsing
   *
   * This is utility function defined here but can be movied to
   * Utils.ts if more usage required.
   */
  initQueryParams() {
    const search = decodeURIComponent( window.location.href.slice( window.location.href.indexOf( '?' ) + 1 ) );
    const definitions = search.split( '&' );

    definitions.forEach((val) => {
      const parts = val.split('=', 2);
      this.queryParams[parts[0]] = parts[1];
    });
  }

  // addEventListener and old browser support
  bindEvent(element, eventName, eventHandler) {
    if (element.addEventListener) {
        element.addEventListener(eventName, eventHandler, false);
    } else if (element.attachEvent) {
        element.attachEvent('on' + eventName, eventHandler);
    }
  }

  initIframeSetup() {
    const me = this;
    // Listen to messages from parent window
    me.bindEvent(window, 'message', (event) => {
      if (event.data.key === 'input') {
        me.doSetup(event.data.value);
      }
    });
    console.log('license-profile-editor: iframe init - send message');
    me.sendMessage({
      key: 'init_iframe',
      value: ''
    });
  }

  // Send a message to the parent
  sendMessage(msgObj) {
    window.parent.postMessage(msgObj, '*');
  }

  ngOnInit() {

    // attempt to read mode value from the query param, if not given as input
    if (!this.mode) {
      this.initQueryParams();
      if (this.queryParams.mode) {
        this.mode = this.queryParams.mode;
      }
    }

    this.doSetup(this.service.getInitialData());

    if (this.mode === 'iframe') {
      this.initIframeSetup();
    }

  }

  doSetup(input: any) {

    const me = this;
    me.errors = [];

    me.service.getComponentInput(input).subscribe((compInput: any) => {

      if (compInput.data
          && compInput.data.keyword
          && compInput.data.licenseName) {
        me.title = 'Modify License Profile';
      } else {
        me.title = 'Create New License Profile';
      }

      me.profileFormInput = {
        schema: compInput.schema,
        layout: compInput.layout,
        data: compInput.data,
        options: {
          addSubmit: false, // Add a submit button if layout does not have one
          //  debug: true, // Don't show inline debugging information
          //   loadExternalAssets: true, // Load external css and JavaScript for frameworks
          //   returnEmptyFields: false, // Don't return values for empty input fields
          setSchemaDefaults: true, // Always use schema defaults for empty fields
          defautWidgetOptions: {
            feedback: true, // Show inline feedback icons
            listItems: 0 // Number of list items to initially add to arrays with no default value
          }
        }
      };
    }, errors => {
      me.errors = errors;
    });

  }

  isValidFn(isValid) {
    this.isValid = isValid;
  }

  @ViewChild(JsonSchemaFormComponent, { static: false })
  set licenseProfileFormCmp(cmp: JsonSchemaFormComponent) {
    if (!this.licenseProfileForm && cmp) {
      this.licenseProfileForm = cmp;
    }
  }

  saveLicenseProfile() {
    const formData = this.getLicenseProfileDataToSave();
    // - post license profile JSON data
    this.sendMessage({
      key: 'output',
      value: formData
    });
  }

  cancelLicenseProfile() {
    this.sendMessage({
      key: 'action',
      value: 'cancel'
    });
  }

  async downloadLicenseProfile(downloadType) {
    const formData = this.getLicenseProfileDataToSave();
    this.download(formData, downloadType);
  }

  getLicenseProfileDataToSave() {
    return this.licenseProfileForm.jsf.validData;
  }

  // create a yaml (text) or json file from the json model
  async download(formData, downloadType) {
    this.downloadType = downloadType;
    // save as json
    let mimeType;
    let data;
    let fileName;
    switch (this.downloadType) {
      case 'json':
        mimeType = { type: 'application/json' };
        data = JSON.stringify(formData);
        fileName = 'license.json';
        break;
      // case 'txt':
      //   fileName = 'LICENSE';
      //   mimeType = { type: 'plain/text' };
      //   const json2yaml = await import('json2yaml');
      //   data = json2yaml.stringify(formData);
      //   break;
    }
    this.downloadFile(data, mimeType, fileName);
  }

  downloadFile(data: any, mimeType: { type: string }, filename: string) {
    const blob = new Blob([data], mimeType);
    const uri = window.URL.createObjectURL(blob);

    const link = document.createElement('a');
    if (typeof link.download === 'string') {
      document.body.appendChild(link); // Firefox requires the link to be in the body
      link.download = filename;
      link.href = uri;
      link.click();
      document.body.removeChild(link); // remove the link when done
    } else {
      location.replace(uri);
    }
  }

}
