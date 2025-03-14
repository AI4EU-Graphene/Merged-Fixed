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

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class LicenseRtuMetaService {

  urlCache: any = {};

  exampleUrlBase: string;

  constructor(private http: HttpClient) {
    this.exampleUrlBase = `assets/exampledata/`;
    // load url cache up with last version of schema
  }


  getRtuAgreementInitialData() {
    return {
      '@context': {
        '@vocab': 'https://www.w3.org/ns/odrl.jsonld#',
        vcard: 'http://www.w3.org/2006/vcard/ns#'
      },
      '@type': 'Agreement',
      $schema: environment.schemaUrl,
      assignee: {}
    };
  }

  getRtuRestrictionsInitialData() {
    return {
      '@context': {
        '@vocab': 'https://www.w3.org/ns/odrl.jsonld#',
        vcard: 'http://www.w3.org/2006/vcard/ns#'
      },
      '@type': 'Agreement',
      $schema: environment.restrictionsSchemaUrl
    };
  }

  getComponentInput(input: any) {

    const me = this;

    return new Observable(subscriber => {

      const compInput = {
        schema: '',
        layout: '',
        data: input
      };
      const errors = [];
      let schemaUrl: string;
      if (input && input.$schema) {
        schemaUrl = input.$schema;
      } else {
        errors.push('The given document is missing $schema field.');
        subscriber.error(errors);
        return;
      }

      const successfulSchema: (value: any) => void = (schema) => {
        // console.log(schema);
        // schema loaded
        compInput.schema = schema;

        // find the respective layout based on the schema version
        let layoutUrl: string;
        if (schema.version) {
          let layoutIdentifier = schema.version;
          if (schema.schemaType) {
            layoutIdentifier += '-' + schema.schemaType;
          }
          layoutUrl = environment.layoutVersionToUrlMap[layoutIdentifier];
        } else {
          errors.push('The schema (' + schemaUrl + ') referenced by the document'
            + ' is missing the version field.');
          subscriber.error(errors);
          return;
        }
        if (layoutUrl) {
          me.getUrlData(layoutUrl).subscribe((layout) => {
            compInput.layout = layout;
            subscriber.next(compInput);
          }, error => {
            console.log('Unable to load layout' + layoutUrl, error);
            errors.push('Unable to load the layout based on the schema version ' + schema.version);
            subscriber.error(errors);
          });
        } else {
          errors.push('Unable to find layout info based on the schema version ' + schema.version);
          errors.push('Please make sure that the document refers to supported schema.');
          subscriber.error(errors);
        }

      };

      me.getUrlData(schemaUrl).subscribe(successfulSchema, error => {

        // fallback to local
        const remoteUrl = new URL(schemaUrl);
        const pathname = remoteUrl.pathname;
        const path = remoteUrl.pathname.substring(pathname.indexOf('asset'), remoteUrl.pathname.length);
        console.log(`loading local path as fallback ${path}`);
        me.getUrlData(path).subscribe(successfulSchema, errorFallback => {
          console.log('Unable to load schema ' + schemaUrl, error);
          console.log('Unable to load schema ' + schemaUrl, errorFallback);
          errors.push('Unable to load schema ' + schemaUrl);
          errors.push('Unable to load schema ' + path);
          errors.push('Please make sure that the $schema URL given in the document is accessible.');
          subscriber.error(errors);
        });
      });

    });
  }

  /**
   * Fetches data (GET request) for the given url
   *
   * @param url url to fetch the data
   */
  getUrlData(url: string): Observable<any> {
    const me = this;
    return new Observable((subscriber) => {
      const urlData = me.urlCache[url];
      if (urlData) {
        subscriber.next(urlData);
      } else {
        // load schema
        me.http.get(`${url}`).subscribe(urlResp => {
          // set it to the local map for reuse
          me.urlCache[url] = urlResp;
          subscriber.next(urlResp);
        }, error => {
          console.log('Error loading ' + url, error);
          subscriber.error(error);
        });
      }
      return { unsubscribe() { } };
    });
  }

  getSample(id: string): any {
    return this
      .http
      .get(`${this.exampleUrlBase}/${id}`);
  }

}
