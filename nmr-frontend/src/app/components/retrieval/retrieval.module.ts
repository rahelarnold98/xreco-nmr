import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {CommonModule, NgOptimizedImage} from "@angular/common";
import {SearchComponent} from "./search/search.component";
import {MatButtonModule} from "@angular/material/button";

@NgModule({
    declarations: [SearchComponent],
    imports: [
        BrowserModule,
        CommonModule,
        NgOptimizedImage,
        MatButtonModule
    ],
    providers: [],
    bootstrap: [SearchComponent]
})
export class RetrievalModule { }