
import $ from 'jquery';
import {appendFadeInHtmlBlock} from '../ui/effects';

const NEXT_OPEN_TAG     = '<next>';
const NEXT_CLOSE_TAG    = '</next>';

export function parseNextPageUrl(htmlPageString, onNextUrlPresentFn, onNextUrlAbsentFn) {
  // next URL should be appended in HTML comment containing next URL enclosed in 'next' tags,
  // it can look as follows:
  // ...(elements)...<!-- <next>/user/items?offset=100&limit=10</next> -->
  const nextStart = htmlPageString.lastIndexOf(NEXT_OPEN_TAG);
  const nextEnd = htmlPageString.lastIndexOf(NEXT_CLOSE_TAG);
  const nextUrlStart = nextStart + NEXT_OPEN_TAG.length;

  if (nextEnd > nextUrlStart) {
    // there is another page
    const nextUrl = htmlPageString.substring(nextUrlStart, nextEnd);
    onNextUrlPresentFn(nextUrl);
  } else {
    onNextUrlAbsentFn();
  }

}

function fetchAndAppendHtml($list, $loadButton, $loadButtons) {
  const pageUrl = $loadButton.attr('next-url');
  const $deferred = $.ajax(pageUrl);

  $deferred.fail(function () {
    console.warn("Error while loading", pageUrl);
  });

  $deferred.done(function (htmlPageString) {
    //console.log("Retrieved htmlPageString ", htmlPageString, " for url", pageUrl);
    parseNextPageUrl(htmlPageString, (nextUrl) => {
      $loadButton.attr('next-url', nextUrl);
    }, () => {
      console.log("nothing to load, removing...");
      // nothing left to load, disable load buttons
      $loadButtons.remove();
    });

    appendFadeInHtmlBlock($list, htmlPageString);
  });
}

function loadMoreHandlerForElement() {
  const targetListSel = $(this).attr('target-list');
  const $targetList = $(targetListSel);
  if ($targetList.length === 0) {
    console.warn('Target list is missing for load button', this);
    return;
  }

  const loadButtonsClass = $(this).attr('load-button-class');
  const $loadButtons = $('.' + loadButtonsClass);
  if ($loadButtons.length === 0) {
    console.warn('Load buttons class is missing for load button', this);
    return;
  }

  fetchAndAppendHtml($targetList, $(this), $loadButtons);
}

function loadDeferredForElement() {
  const $container = $(this);
  const deferredLoadUrl = $(this).attr('deferred-load-url');
  const $deferred = $.ajax(deferredLoadUrl);

  $deferred.fail(function () {
    console.warn("Error while loading", deferredLoadUrl);
  });

  $deferred.done(function (htmlString) {
    const $element = $($.parseHTML(htmlString));
    $element.appendTo($container);
  });
}

export function setUpPaginationHandlers() {
  $('.deferred-load').each(loadDeferredForElement);

  // Note: 'on' used here to assign handlers for dynamically created elements
  $(document).on('click', '.load-more', loadMoreHandlerForElement);
}
