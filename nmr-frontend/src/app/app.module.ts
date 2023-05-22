import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import {RetrievalModule} from "./components/retrieval/retrieval.module";
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import {HttpClientModule} from "@angular/common/http";
import {ApiModule, Configuration} from "../../openapi";

/**
 * Provides the {@link AppConfig} reference.
 *
 * @param appConfig Reference (provided by DI).
 */
export function initializeApiConfig() {
  return new Configuration({ basePath: "http://localhost:7070", withCredentials: true });
}


@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    {ngModule: ApiModule, providers: [{ provide: Configuration, useFactory: initializeApiConfig }] },

    BrowserModule,
    HttpClientModule,
    AppRoutingModule,

    /* Our own modules. */
    RetrievalModule,
    BrowserAnimationsModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
