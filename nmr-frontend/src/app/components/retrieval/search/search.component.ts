import {Component} from "@angular/core";
import {RetrievalService} from "../../../../../openapi";
import {ActivatedRoute, Router} from "@angular/router";
import {BehaviorSubject} from "rxjs";
import {ScoredMedia} from "../../../../../openapi/model/scoredMedia";
import { trigger, state, style, animate, transition } from '@angular/animations';

@Component({
    selector: 'nmr-search',
    templateUrl: './search.component.html',
    styleUrls: ['./search.component.scss'],
    animations: [
        trigger('fadeInOut', [
            state('void', style({ opacity: 0 })),
            transition(':enter, :leave', [
                animate(300)
            ])
        ])
    ]
})
export class SearchComponent {

    /** The field holding the search input. */
    searchInput: string = '';

    /** */
    searchIssued: boolean = false;

    /** The currently active page. */
    private _items = new BehaviorSubject<Array<ScoredMedia>>([])

    /** The currently active page. */
    private _page = new BehaviorSubject<number>(0)

    /** The total number of items returned by the current query. */
    private _count = new BehaviorSubject<number>(0)

    constructor(
        private router: Router,
        private routes: ActivatedRoute,
        private service: RetrievalService
    ) {
    }


    /**
     * Getter for the items.
     */
    get items() {
        return this._items.asObservable()
    }

    /**
     *
     * @param text
     */
    public search() {
        this.searchIssued = true
        this.service.getFullTextQuery("features_landmark", this.searchInput,50, 0).subscribe(result => {
            this._items.next(result.items);
            this._page.next(result.page);
            this._count.next(result.count);
        })
    }
}