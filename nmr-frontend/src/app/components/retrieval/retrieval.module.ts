import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {CommonModule, NgOptimizedImage} from "@angular/common";
import {SearchComponent} from "./search/search.component";
import {MatButtonModule} from "@angular/material/button";
import {FormsModule} from "@angular/forms";

@NgModule({
    declarations: [SearchComponent],
    imports: [
        BrowserModule,
        CommonModule,
        NgOptimizedImage,
        MatButtonModule,
        FormsModule
    ],
    providers: [],
    bootstrap: [SearchComponent]
})
export class RetrievalModule { }