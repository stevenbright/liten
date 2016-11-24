
import $ from 'jquery';

const NEXT_OPEN_TAG     = '<next>';
const NEXT_CLOSE_TAG    = '</next>';

function appendHtml($container, htmlString) {
  // NOTE:  two approaches are possible here, one uses jquery abstractions (and works on pretty old browsers),
  //        while the other uses insertAdjacentHTML, the standard API in all the modern browsers (and old IEs too!)
  //
  // First approach (jquery way):
  //        const $element = $($.parseHTML(htmlString));
  //        $element.appendTo($container);
  //
  // Other approach (pure HTML):
  //        $container.each(function () { this.insertAdjacentHTML('beforeend', htmlString); });
  //
  //
  // Links:
  //    https://developer.mozilla.org/en-US/docs/Web/API/Element/insertAdjacentHTML
  //    https://msdn.microsoft.com/en-us/library/ms536452(v=vs.85).aspx

  $container.each(function () {
    this.insertAdjacentHTML('beforeend', htmlString);
  });
}

function fetchAndAppendHtml($list, $loadButton) {
  const pageUrl = $loadButton.attr('next-url');
  const $deferred = $.ajax(pageUrl);

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

    appendHtml($list, htmlPageString);
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
