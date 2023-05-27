import {Component} from "@angular/core";
import {BasketPreview, BasketService, RetrievalService, ScoredMediaItem} from "../../../../../openapi";
import {ActivatedRoute, Router} from "@angular/router";
import {BehaviorSubject} from "rxjs";
import { trigger, state, style, animate, transition } from '@angular/animations';
import {SimpleInputDialogComponent} from "../../general/simple-input-dialog.component";
import {MatDialog, MatDialogConfig} from "@angular/material/dialog";
import {DetailsComponent} from "../details/details.component";

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

    /** A flag indicating, that a search has been issued. */
    searchIssued: boolean = false;

    /** The currently active page. */
    private _baskets = new BehaviorSubject<Array<BasketPreview>>([])
    public baskets = this._baskets.asObservable()

    /** The currently active basket. */
    private _activeBasket = new BehaviorSubject<BasketPreview|null>(null)
    public activeBasket = this._activeBasket.asObservable()


    /** The currently active page. */
    private _items = new BehaviorSubject<Array<ScoredMediaItem>>([])

    /** The currently active page. */
    private _page = new BehaviorSubject<number>(0)

    /** The total number of items returned by the current query. */
    private _count = new BehaviorSubject<number>(0)

    /** The total number of items returned by the current query. */
    private _bestScore = new BehaviorSubject<number>(0.0)


    /** */
    public focus: ScoredMediaItem | null = null

    constructor(
        private router: Router,
        private routes: ActivatedRoute,
        private dialog: MatDialog,
        private service: RetrievalService,
        private basket: BasketService
    ) {
       this.reloadBaskets()
    }

    /**
     * Getter for the items.
     */
    get items() {
        return this._items.asObservable()
    }

    /**
     *
     */
    get bestScore(): number {
        return this._bestScore.value
    }

    /**
     * Returns the image path for the given item.
     *
     * @param item {@link ScoredMediaItem} to return path for.
     */
    public pathForItem(item: ScoredMediaItem): string {
        return `${this.service.configuration.basePath}/api/resource/${item.id}/preview/${item.start}`
    }

    /**
     *
     * @param item
     */
    public showDetails(item: ScoredMediaItem) {
       this.dialog.open(DetailsComponent, {width: '600px', data: { id: item.id, timestamp: item.start }} as MatDialogConfig);
    }

    /**
     * Issues a fulltext search using the provided text input.
     */
    public search() {
        this.searchIssued = true
        this.service.getSearchFulltext("features_landmark", this.searchInput,500, 0).subscribe(result => {
            if (result.items.length > 0) {
                this._bestScore.next(result.items[0].score)
            } else {
                this._bestScore.next(0)
            }
            this._items.next(result.items);
            this._page.next(result.page);
            this._count.next(result.count);
        })
    }

    /**
     * Issues a similarity search using the provided {@link ScoredMediaItem}.
     *
     * @param item The {@link ScoredMediaItem} to search similar items for.
     */
    public searchSimilar(item: ScoredMediaItem) {
        this.searchIssued = true
        this.service.getSearchSimilar("features_clip", item.id, item.start ? 0 : item.start!, 500, 0).subscribe(result => {
            if (result.items.length > 0) {
                this._bestScore.next(result.items[0].score)
            } else {
                this._bestScore.next(0)
            }
            this._items.next(result.items);
            this._page.next(result.page);
            this._count.next(result.count);
        })
    }

    /**
     * Opens a dialog that can be used to provide a name and creates a new basket.
     */
    public createBasket() {
        const dialogRef = this.dialog.open(SimpleInputDialogComponent, {data: { title: 'Create new basket' }} as MatDialogConfig);
        dialogRef.afterClosed().subscribe(result => {
            this.basket.postBasket(result).subscribe(s => this.reloadBaskets())
        });
    }

    /**
     * Opens a dialog that can be used to provide a name and creates a new basket.
     */
    public deleteBasket() {
        if (this._activeBasket.value != null) {
            this.basket.deleteBasket(this._activeBasket.value?.basketId!!).subscribe(s => this.reloadBaskets())
        }
    }


    /**
     * Switches the currently active {@link BasketPreview}.
     *
     * @param basket The new {@link BasketPreview} to select.
     */
    public switchBasket(basket: BasketPreview) {
        this._activeBasket.next(basket)
    }

    /**
     * Adds the selected {@link ScoredMediaItem} to the active {@link Basket}.
     *
     * @param item The {@link ScoredMediaItem} to add.
     */
    public addToBasket(item: ScoredMediaItem) {
        if (this._activeBasket.value != null) {
            this.basket.putToBasket(this._activeBasket.value?.basketId, item.id).subscribe(s => this.reloadBaskets())
        }
    }

    /**
     * Reloads basket information.
     */
    public reloadBaskets() {
        this.basket.getAllBaskets().subscribe(l => {
            this._baskets.next(l.baskets)
            let activeBasket = l.baskets.find(b => b.name == this._activeBasket.value?.name)
            if (activeBasket) {
                this._activeBasket.next(activeBasket)
            } else if (l.baskets.length > 0) {
                this._activeBasket.next(l.baskets[0])
            } else {
                this._activeBasket.next(null)
            }
        })
    }
}