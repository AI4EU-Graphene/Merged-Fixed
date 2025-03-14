/*-
 * ===============LICENSE_START================================================
 * Acumos Apache-2.0
 * ============================================================================
 * Copyright (C) 2019 Nordix Foundation.
 * Modifications copyright (C)2020 Tech Mahindra
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

import { Component, OnInit, ViewChild, Input, ElementRef } from "@angular/core";
import { MatIconRegistry } from "@angular/material/icon";
import { LicenseRtuMetaService } from "../license-rtu-meta.service";
import { JsonSchemaFormComponent } from "@earlyster/angular6-json-schema-form";
import { APP_VERSION } from '../../environments/app.version';
import { MatSnackBar } from "@angular/material/snack-bar";
import { environment } from "../../environments/environment";
import { DomSanitizer } from "@angular/platform-browser";

export enum ViewType {
  SUP = "SUP", // supplier
  SUB = "SUB" // subscriber
}

export enum AssetUsageAgreementSource {
  LUM = "LUM", // LUM
  FILE = "FILE" // FILE
}

@Component({
  selector: "app-license-rtu-editor",
  templateUrl: "./license-rtu-editor.component.html",
  styleUrls: ["./license-rtu-editor.component.scss"]
})

/**
 * RTU form editor
 * Will construct a minimal ODRL RTU file for
 * use by LUM.
 */
export class LicenseRtuEditorComponent implements OnInit {
  title = "Acumos Right to Use Editor";
  appVersion = APP_VERSION.app_git_version;
  isValid: any;
  downloadType = "txt";
  errors: string[];
  rtuFormInput: any;
  rtuEditorForm: JsonSchemaFormComponent;
  softwareLicensorIdEl: ElementRef;
  assetUsageAgreementIdEl: ElementRef;
  queryParams: any = {};
  lumServerUrl = "/lum";
  viewType = ViewType.SUP;
  rtuLumAgreement: any;
  reviewRtuAgreementChecked: boolean;
  userId: string;

  @Input() mode: string;

  constructor(
    private matIconRegistry: MatIconRegistry,
    private domSanitizer: DomSanitizer,
    private service: LicenseRtuMetaService,
    private snackBar: MatSnackBar
  ) {
    this.matIconRegistry.addSvgIcon(
      `close`,
      this.domSanitizer.bypassSecurityTrustResourceUrl("assets/icons/close.svg")
    );

    if (!environment.production) {
      this.lumServerUrl = "http://localhost:2080";
    }
    this.resetData();
  }

  /**
   * JavaScript Get URL Parameter parsing
   *
   * This is utility function defined here but can be movied to
   * Utils.ts if more usage required.
   */
  initQueryParams() {
    const search = decodeURIComponent(
      window.location.href.slice(window.location.href.indexOf("?") + 1)
    );
    const definitions = search.split("&");

    definitions.forEach((val) => {
      const parts = val.split("=", 2);
      this.queryParams[parts[0]] = parts[1];
    });
  }

  // addEventListener and old browser support
  bindEvent(element, eventName, eventHandler) {
    if (element.addEventListener) {
      element.addEventListener(eventName, eventHandler, false);
    } else if (element.attachEvent) {
      element.attachEvent("on" + eventName, eventHandler);
    }
  }

  initIframeSetup() {
    const me = this;
    // Listen to messages from parent window
    me.bindEvent(window, "message", (event) => {
      if (event.data.key === "input") {
        me.doSetup(event.data.value);
      }
    });
    console.log("license-rtu-editor: iframe init - send message");
    me.sendMessage({
      key: "init_iframe",
      value: ""
    });
  }

  // Send a message to the parent
  sendMessage(msgObj) {
    window.parent.postMessage(msgObj, "*");
  }

  ngOnInit() {
    const me = this;
    // attempt to read mode value from the query param, if not given as input
    if (!me.mode) {
      me.initQueryParams();
      if (me.queryParams.mode) {
        me.mode = me.queryParams.mode;
      }
    }

    me.initSetup();

    if (me.mode === "iframe") {
      me.initIframeSetup();
    }
  }

  initSetup() {
    const me = this;
    me.doSetup(
      me.viewType === ViewType.SUB
        ? me.service.getRtuRestrictionsInitialData()
        : me.service.getRtuAgreementInitialData()
    );
  }

  resetData() {
    const me = this;
    me.errors = [];
    me.rtuFormInput = undefined;
    const rtuLumAgreementSrc =
      me.rtuLumAgreement && me.rtuLumAgreement.src
        ? me.rtuLumAgreement.src
        : "LUM";
    me.rtuLumAgreement = {
      src: rtuLumAgreementSrc,
      assetUsageAgreement: {}
    };
    if (me.softwareLicensorIdEl) {
      me.softwareLicensorIdEl.nativeElement.value = "";
    }
    if (me.assetUsageAgreementIdEl) {
      me.assetUsageAgreementIdEl.nativeElement.value = "";
    }
    me.reviewRtuAgreementChecked = false;
    me.userId = "";
  }

  resetSetup() {
    const me = this;
    me.resetData();
    setTimeout(() => {
      me.initSetup();
    }, 1);
  }

  doSetup(input: any, options?: any) {
    const me = this;
    me.errors = [];

    me.service.getComponentInput(input).subscribe(
      (compInput: any) => {
        const formInputObj: any = {
          schema: compInput.schema,
          layout: compInput.layout,
          data: compInput.data,
          options: {
            addSubmit: false, // Add a submit button if layout does not have one
            // debug: true, // Don't show inline debugging information
            // loadExternalAssets: true, // Load external css and JavaScript for frameworks
            // returnEmptyFields: false, // Don't return values for empty input fields
            setSchemaDefaults: true, // Always use schema defaults for empty fields
            defautWidgetOptions: {
              feedback: true, // Show inline feedback icons
              listItems: 0 // Number of list items to initially add to arrays with no default value
            }
          }
        };
        if (options) {
          if (options.defautWidgetOptions) {
            Object.assign(
              formInputObj.options.defautWidgetOptions,
              options.defautWidgetOptions
            );
          }
          if (options.isExampleData) {
            formInputObj.isExampleData = true;
          }
        }
        me.rtuFormInput = formInputObj;
      },
      (errors) => {
        me.errors = errors;
      }
    );
  }

  onChangeViewType() {
    this.resetSetup();
  }

  onChangeAssetUsageAgreementSource() {
    this.resetData();
  }

  onChangeReviewRtuAgreement() {
    this.applyRTULumAgreement(this.reviewRtuAgreementChecked);
  }

  onChangeUserId() {
    this.canEnableSaveBtn();
  }

  showExample(id: string) {
    const me = this;
    me.service.getSample(id).subscribe((data: any) => {
      me.rtuEditorForm.formInitialized = false;
      me.doSetup(data, {
        isExampleData: true
      });
    });
  }

  onImportAgreementFile(event: any) {
    if (!event || !event.target || !event.target.files) {
      return;
    }
    const me = this;
    me.errors = [];
    if (typeof FileReader !== "undefined") {
      const reader = new FileReader();

      reader.onload = (e: any) => {
        const dataInput = e.target.result;
        me.rtuEditorForm.formInitialized = false;
        try {
          const rtu = JSON.parse(dataInput);
          me.resetData();
          me.doSetup(rtu, {
            isExampleData: true
          });
          // reset input file field value so that attempting
          // to import same file result in change notification
          event.target.value = "";
        } catch (e) {
          me.errors.push("Failed reading / parsing the input file");
        }
      };

      reader.readAsText(event.target.files[0]);
    }
  }

  onImportAssetUsageAgreementFile(event: any) {
    if (!event || !event.target || !event.target.files) {
      return;
    }
    const me = this;
    me.errors = [];
    if (typeof FileReader !== "undefined") {
      const reader = new FileReader();

      reader.onload = (e: any) => {
        const dataInput = e.target.result;
        me.rtuEditorForm.formInitialized = false;
        try {
          // me.resetData();
          const agreementData = JSON.parse(dataInput);

          Object.assign(me.rtuLumAgreement, agreementData);
          // me.rtuLumAgreement.src = AssetUsageAgreementSource.FILE;
          me.reviewRtuAgreementChecked = true;
          me.applyRTULumAgreement(true);

          // reset input file field value so that attempting
          // to import same file result in change notification
          event.target.value = "";
        } catch (e) {
          me.errors.push("Failed reading / parsing the input file");
        }
      };

      reader.readAsText(event.target.files[0]);
    }
  }

  applyRTULumAgreement(showReviewRtuAgreement?: boolean) {
    const me = this;
    const data = me.rtuLumAgreement && me.rtuLumAgreement.assetUsageAgreement;

    // prepare input based on agreementRestriction availibility
    if (showReviewRtuAgreement && data && data.agreement) {
      const options: any = {
        defautWidgetOptions: {
          // For readonly form
          addable: false,
          removable: false,
          readonly: true
        }
      };
      me.doSetup(data.agreement, options);
    } else if (data && data.agreementRestriction) {
      me.doSetup(data.agreementRestriction);
    } else {
      me.initSetup();
    }
  }

  isValidFn(isValid: boolean) {
    const me = this;
    me.isValid = isValid;
  }

  canEnableGetRTUAgreementDataBtn() {
    const me = this;
    if (
      me.viewType === ViewType.SUB &&
      me.rtuLumAgreement.src === AssetUsageAgreementSource.LUM
    ) {
      if (
        me.lumServerUrl &&
        me.softwareLicensorIdEl &&
        me.softwareLicensorIdEl.nativeElement.value &&
        me.assetUsageAgreementIdEl &&
        me.assetUsageAgreementIdEl.nativeElement.value
      ) {
        return true;
      }
    }
    return false;
  }

  /**
   * Disabled state for Save button
   * returns true, if
   *   - no lumServerUrl
   *   - form is not valid
   *   - no userId
   */
  canEnableSaveBtn() {
    const me = this;
    if (
      me.rtuLumAgreement.src === AssetUsageAgreementSource.LUM &&
      me.reviewRtuAgreementChecked
    ) {
      return false;
    }
    if (me.lumServerUrl && me.isValid && me.userId) {
      if (
        me.softwareLicensorIdEl &&
        me.softwareLicensorIdEl.nativeElement.value &&
        me.assetUsageAgreementIdEl &&
        me.assetUsageAgreementIdEl.nativeElement.value
      ) {
        return true;
      }
    }
    return false;
  }

  @ViewChild(JsonSchemaFormComponent, { static: false })
  set rtuEditorFormCmp(cmp: JsonSchemaFormComponent) {
    if (cmp) {
      this.rtuEditorForm = cmp;
    }
  }

  @ViewChild("softwareLicensorId", { static: false })
  set softwareLicensorIdInput(inputEl: ElementRef) {
    if (inputEl) {
      this.softwareLicensorIdEl = inputEl;
    }
  }

  @ViewChild("assetUsageAgreementId", { static: false })
  set assetUsageAgreementIdInput(inputEl: ElementRef) {
    if (inputEl) {
      this.assetUsageAgreementIdEl = inputEl;
    }
  }

  saveRTU() {
    const me = this;
    const formData = me.getLicenseRtuDataToSave();
    me.putData(formData);
  }

  getRTUAgreementData(
    softwareLicensorId: string,
    assetUsageAgreementId: string
  ) {
    const me = this;
    me.resetData();
    me.fetchRtuAgreementData(softwareLicensorId, assetUsageAgreementId);
  }

  cancelRTU() {
    this.sendMessage({
      key: "action",
      value: "cancel"
    });
  }

  async downloadRTU() {
    const formData = this.getLicenseRtuDataToSave();
    this.download(formData);
  }

  addLumWrapperObjects(formData: any) {
    // add wrapper object
    const me = this;

    if (me.viewType === ViewType.SUB && me.rtuLumAgreement.src === "LUM") {
      const assetUsageAgreement = me.rtuLumAgreement.assetUsageAgreement;
      const reqPayload = {
        assetUsageAgreement: {
          softwareLicensorId: assetUsageAgreement.softwareLicensorId,
          assetUsageAgreementId: assetUsageAgreement.assetUsageAgreementId,
          agreementRestriction: formData
        },
        // additional data prep
        requestId: me.uuidv4(),
        requested: new Date().toISOString(),
        userId: me.userId
      };

      return reqPayload;
    } else {
      // add wrapper object
      const licensor = formData.assigner["vcard:fn"];
      const agreementId = formData.uid;
      return {
        assetUsageAgreement: {
          softwareLicensorId: licensor,
          assetUsageAgreementId: agreementId,
          // supplier agreement
          agreement: formData
        },
      };
    }
  }

  getLicenseRtuDataToSave() {
    const me = this;
    const formData = this.rtuEditorForm.jsf.validData;
    let lumRtuData: any;
    if (formData) {
      const dataToSave = Object.assign({}, formData);
      me.addUIDIfMissing(dataToSave);
      lumRtuData = me.addLumWrapperObjects(dataToSave);
    }
    return lumRtuData;
  }

  /**
   * Auto generates the uid need by LUM
   * Makes sure that the values are encoded
   * @param formData agreement data
   */
  addUIDIfMissing(formData: any) {
    const me = this;
    // add uid to: agreement
    // get company name of software licensor from Assigner
    let agreementData = formData;
    if (
      me.rtuLumAgreement &&
      me.rtuLumAgreement.assetUsageAgreement &&
      me.rtuLumAgreement.assetUsageAgreement.agreement
    ) {
      agreementData = me.rtuLumAgreement.assetUsageAgreement.agreement;
    }
    const companyName = agreementData.assigner["vcard:fn"];
    let type = "";
    if (!formData.uid || me.rtuFormInput.isExampleData) {
      type = "agreement";
      formData.uid = this.createUID(type, companyName);
    }

    if (formData.permission) {
      formData.permission.forEach((rule) => {
        if (!rule.uid || me.rtuFormInput.isExampleData) {
          rule.uid = this.createUID("permission", companyName);
        }
      });
    }

    if (formData.prohibition) {
      formData.prohibition.forEach((rule) => {
        if (!rule.uid || me.rtuFormInput.isExampleData) {
          rule.uid = this.createUID("prohibition", companyName);
        }
      });
    }
  }

  createUID(type: string, companyName: string): string {
    return `acumos://software-licensor/${encodeURIComponent(
      companyName
    )}/${type}/${this.uuidv4()}`;
  }

  uuidv4() {
    // tslint:disable-next-line:whitespace
    return (([1e7] as any) + -1e3 + -4e3 + -8e3 + -1e11).replace(
      /[018]/g,
      (c: any) =>
        // tslint:disable-next-line:no-bitwise
        (
          c ^
          (crypto.getRandomValues(new Uint8Array(1))[0] & (15 >> (c / 4)))
        ).toString(16)
    );
  }

  // create a yaml (text) or json file from the json model
  async download(formData: any) {
    // save as json
    // const mimeType = { type: 'application/json' };
    const data = JSON.stringify(formData);
    const fileName = "rtu-asset-usage-agreement.json";

    this.downloadFile(data, undefined, fileName);
  }

  async putData(formData: any) {
    const me = this;
    let url = me.lumServerUrl + "/api/v1/asset-usage-agreement";

    if (me.rtuLumAgreement.src === AssetUsageAgreementSource.LUM) {
      // currently showing RTU LUM agreement and
      // requesting to save restrictions
      url += "-restriction";
    }
    url +=
      "/?softwareLicensorId=" + formData.assetUsageAgreement.softwareLicensorId;
    url +=
      "&assetUsageAgreementId=" +
      formData.assetUsageAgreement.assetUsageAgreementId;
    try {
      // Default options are marked with *
      const response = await fetch(url, {
        method: "PUT", // *GET, POST, PUT, DELETE, etc.
        mode: "cors", // no-cors, *cors, same-origin
        cache: "no-cache", // *default, no-cache, reload, force-cache, only-if-cached
        credentials: "same-origin", // include, *same-origin, omit
        headers: {
          "Content-Type": "application/json"
        },
        redirect: "follow", // manual, *follow, error
        referrer: "no-referrer", // no-referrer, *client
        body: JSON.stringify(formData) // body data type must match "Content-Type" header
      });
      const data = await response.json(); // parses JSON response into native JavaScript objects
      if (!data || data.error) {
        me.showSnackBar("Failed to save RTU document to LUM server", "Retry");
      } else {
        me.showSnackBar("RTU document saved to LUM server");
      }
      return data;
    } catch (err) {
      me.showSnackBar("Failed to save RTU document to LUM server", "Retry");
    }
  }

  async fetchRtuAgreementData(
    softwareLicensorId: string,
    assetUsageAgreementId: string
  ) {
    const me = this;
    let url = me.lumServerUrl + "/api/v1/asset-usage-agreement";
    url += "/?softwareLicensorId=" + softwareLicensorId;
    url += "&assetUsageAgreementId=" + assetUsageAgreementId;
    try {
      // Default options are marked with *
      const response = await fetch(url, {
        method: "GET", // *GET, POST, PUT, DELETE, etc.
        mode: "cors", // no-cors, *cors, same-origin
        cache: "no-cache", // *default, no-cache, reload, force-cache, only-if-cached
        credentials: "same-origin", // include, *same-origin, omit
        redirect: "follow", // manual, *follow, error
        referrer: "no-referrer", // no-referrer, *client
      });
      const data = await response.json(); // parses JSON response into native JavaScript objects
      if (!data || data.error) {
        me.showSnackBar(
          "Failed to fetch RTU document from LUM server",
          "Retry"
        );
      } else {
        const agreementData = data;

        // initialize agreementRestriction
        //   - copy agreement.uid
        //   - copy permission and prohibitions from agreement
        if (agreementData && agreementData.assetUsageAgreement) {
          const usageAgreement = agreementData.assetUsageAgreement;
          const agreement: any = usageAgreement.agreement;

          let agreementRestriction: any = usageAgreement.agreementRestriction;
          if (!agreementRestriction) {
            agreementRestriction = me.service.getRtuRestrictionsInitialData();
            usageAgreement.agreementRestriction = agreementRestriction;
            agreementRestriction.uid = agreement.uid;
          }
          if (!agreementRestriction.permission && agreement.permission) {
            agreementRestriction.permission = [];
            for (const permissionItem of agreement.permission) {
              agreementRestriction.permission.push(
                JSON.parse(JSON.stringify(permissionItem))
              );
            }
          }
          if (!agreementRestriction.prohibition && agreement.prohibition) {
            agreementRestriction.prohibition = [];
            for (const prohibitionItem of agreement.prohibition) {
              agreementRestriction.prohibition.push(
                JSON.parse(JSON.stringify(prohibitionItem))
              );
            }
          }
        }

        Object.assign(me.rtuLumAgreement, agreementData);
        me.applyRTULumAgreement();
      }
    } catch (err) {
      me.showSnackBar("Failed to fetch RTU document from LUM server", "Retry");
    }
  }

  showSnackBar(message: string, action: string = "") {
    this.snackBar.open(message, action, {
      duration: 3000,
      panelClass: "acumos-snackbar"
    });
  }

  downloadFile(
    data: any,
    mimeType: { type: string } = { type: "application/json" },
    filename: string
  ) {
    const blob = new Blob([data], mimeType);
    const uri = window.URL.createObjectURL(blob);

    const link = document.createElement("a");
    if (typeof link.download === "string") {
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
