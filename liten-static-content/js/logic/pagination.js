
import $ from 'jquery';
import {appendFadeInHtmlBlock} from '../ui/effects';

const NEXT_OPEN_TAG     = '<next>';
const NEXT_CLOSE_TAG    = '</next>';

function fetchAndAppendHtml($list, $loadButton) {
  const pageUrl = $loadButton.attr('next-url');
  const $deferred = $.ajax(pageUrl);

  $deferred.fail(function () {
    console.warn("Error while loading", pageUrl);
  });

  $deferred.done(function (htmlPageString) {
    //console.log("Retrieved htmlPageString ", htmlPageString, " for url", pageUrl);

    // next URL should be appended in HTML comment containing next URL enclosed in 'next' tags,
    // it can look as follows:
    // ...(elements)...<!-- <next>/user/items?offset=100&limit=10</next> -->
    const nextStart = htmlPageString.lastIndexOf(NEXT_OPEN_TAG);
    const nextEnd = htmlPageString.lastIndexOf(NEXT_CLOSE_TAG);
    const nextUrlStart = nextStart + NEXT_OPEN_TAG.length;

    if (nextEnd > nextUrlStart) {
      // there is another page
      const nextUrl = htmlPageString.substring(nextUrlStart, nextEnd);
      //console.log('Next url:', nextUrl);

      $loadButton.attr('next-url', nextUrl);
    } else {
      // nothing left to load, disable load button
      $loadButton.remove();
    }

    appendFadeInHtmlBlock($list, htmlPageString);
  });
}

export function setUpPaginationHandlers() {
  $('.load-more').click(function () {
    const targetListSel = $(this).attr('target-list');
    const $targetList = $(targetListSel);
    if ($targetList.length === 0) {
      console.warn('Target list is missing for load button', this);
      return;
    }

    fetchAndAppendHtml($targetList, $(this));
  });
}
